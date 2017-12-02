package com.woodpigeon.sapling.web

import org.scalajs.dom._
import scala.scalajs.js.timers._

case class Vector(x: Double = 0, y: Double = 0)

case class SproutContext(sap: Double)

case class ExtendContext(v0: Vector, aB: Double)


trait Sprouting {
  def extend(ctx: ExtendContext): Option[Extended]
}

trait Extended {
  def sprout(ctx: SproutContext) : Option[Sprouting]
}


case class ExtendedBranch(branch: SproutingBranch, v0: Vector, v1: Vector, a: Double, children: List[Extended]) extends Extended {

  private val growthRate = 0.7
  private val stature = 10

  override def sprout(ctx: SproutContext): Option[Sprouting] = branch match {
    case SproutingBranch(_, _, g)  =>
      Some(branch.copy(
        g = g + (growthRate - (growthRate * Math.cos(2 * Math.sqrt((10 / stature) * g)))) / 2,
        children = {
          val grownChildren = children flatMap { _.sprout(SproutContext(1)) }

          if(children.isEmpty) {
            if(g > 8 && Math.random() > 0.97) {
              SproutingBranch(-0.3 * a) :: grownChildren
            }
            else grownChildren
          }
          else {
            if(Math.random() > 0.99) {
              val childAngle = if(Math.random() > 0.5) Math.PI / 2 else -Math.PI / 2
              SproutingBranch(childAngle) :: grownChildren
            }
            else grownChildren
          }
        }
      ))
  }

}


case class SproutingBranch(aOrig: Double, children: List[Sprouting] = Nil, g: Double = 0.2) extends Sprouting {

  override def extend(ctx: ExtendContext): Option[Extended] = ctx match {
    case ExtendContext(v0@Vector(x0, y0), aBase) =>
      val a = (aBase + aOrig) % Math.PI

      val v1 = Vector(
        x0 + (Math.sin(a) * g),
        y0 + (Math.cos(a) * g)
      )

      Some(ExtendedBranch(this, v0, v1, a, children.flatMap { _.extend(ExtendContext(v1, a)) }))
  }

}







case class RenderContext(offset: Vector, first: Boolean = true)


object Main {

  def render(ctx: RenderContext, el: Extended) : String = ctx match {
    case RenderContext(offset, isFirst) =>
      el match {
        case ExtendedBranch(_, v0, v1, _, children) => {
          val inner = (children map {
            render(RenderContext(offset, false), _)
          }).mkString

          (v0, v1) match {
            case (Vector(x0, y0), Vector(x1, y1)) => {
              val ox0 = offset.x + x0
              val oy0 = offset.y - y0
              val ox1 = offset.x + x1
              val oy1 = offset.y - y1

              if (isFirst)
                s"M$ox0 $oy0 L$ox1 $oy1 ${inner}L$ox0 $oy0"
              else
                s"L$ox1 $oy1 ${inner}L$ox0 $oy0 "
            }
          }
        }
      }
  }



  def main() : Unit = {

    val elPath = document.getElementById("treePath").asInstanceOf[svg.Path]

    var sprouting: Option[Sprouting] = Some(SproutingBranch(0))
    var extended: Option[Extended] = None

    println("Starting!")


    setInterval(100) {
      for(_ <- 1 to 20) {
        extended = sprouting flatMap { _.extend(ExtendContext(Vector(), 0)) }
        sprouting = extended flatMap { _.sprout(SproutContext(1)) }
      }

      extended foreach { tree =>
        val pathData = render(RenderContext(Vector(400, 500)), tree)
        elPath.setAttribute("d", pathData)
      }
    }

  }

}
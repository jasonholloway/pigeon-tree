package com.woodpigeon.sapling.web

import org.scalajs.dom._
import scala.scalajs.js.timers._

case class Vector(x: Double = 0, y: Double = 0)

case class GrowContext(sap: Double)

case class ProjectContext(v0: Vector, aB: Double)


trait Sprouting {
  def project(ctx: ProjectContext): Option[Extended]
}

trait Extended {
  def grow(ctx: GrowContext) : Option[Sprouting]
}


case class ExtendedBranch(branch: SproutingBranch, v0: Vector, v1: Vector, a: Double, children: List[Extended]) extends Extended {

  private val growthRate = 0.8
  private val stature = 10

  override def grow(ctx: GrowContext): Option[Sprouting] = branch match {
    case SproutingBranch(_, _, _, g, version)  =>
      Some(branch.copy(
        version = version + 1,
        g = g + (growthRate - (growthRate * Math.cos(2 * Math.sqrt((10 / stature) * g)))) / 2,
        children = {
          val grownChildren = children flatMap { _.grow(GrowContext(1)) }

          if(children.isEmpty) {
            if(g > 7 && Math.random() > 0.97) {
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


case class SproutingBranch(aOrig: Double, children: List[Sprouting] = Nil, next: Option[Sprouting] = None, g: Double = 0.1, version: Int = 0) extends Sprouting {

  override def project(ctx: ProjectContext): Option[Extended] = ctx match {
    case ProjectContext(v0@Vector(x0, y0), aBase) =>
      val a = (aBase + aOrig) % Math.PI

      val v1 = Vector(
        x0 + (Math.sin(a) * g),
        y0 + (Math.cos(a) * g)
      )

      Some(ExtendedBranch(this, v0, v1, a, children.flatMap { _.project(ProjectContext(v1, a)) }))
  }

}







case class RenderContext(offset: Vector, first: Boolean = false)


object Main {

  def render(ctx: RenderContext, el: Extended) : String = ctx match {
    case RenderContext(offset, isFirst) =>
      el match {
        case ExtendedBranch(_, v0, v1, _, children) => {
          val inner = (children map {
            render(RenderContext(offset), _)
          }).mkString

          (v0, v1) match {
            case (Vector(x0, y0), Vector(x1, y1)) => {
              val ox0 = offset.x + x0
              val oy0 = offset.y - y0
              val ox1 = offset.x + x1
              val oy1 = offset.y - y1

              if (isFirst)
                s"M$ox0 $oy0 ${inner}L$ox0 $oy0"
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
    var i = 0

    println("Starting!")


    setInterval(20) {
      extended = sprouting flatMap { _.project(ProjectContext(Vector(), 0)) }

      sprouting = extended flatMap { _.grow(GrowContext(1)) }

      if (i % 8 == 0) {
        extended foreach { e =>
          val pathData = render(RenderContext(Vector(400, 500), first = true), e)
          elPath.setAttribute("d", pathData)
        }
      }

      i = i + 1
    }

  }

}
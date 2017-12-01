package com.woodpigeon.sapling.web

import org.scalajs.dom._
import scala.scalajs.js.timers._

case class Vector(x: Double, y: Double)


case class GrowContext(baseX: Double, baseY: Double, baseA: Double)

trait Growable {
  def grow(ctx: GrowContext) : Option[Growable]
}

case class Branch(
  angle: Double,
  start: Vector,
  end: Vector,
  children: List[Growable] = Nil,
  next: Option[Growable] = None,
  g: Double = 0.1,
  version: Int = 0) extends Growable {

  private val d = 0.7
  private val l = 10

  def grow(ctx: GrowContext): Option[Growable] = ctx match {
    case GrowContext(x0, y0, baseA) => {
      val a = (baseA + angle) % Math.PI

      val x1 = x0 + (Math.sin(a) * g)
      val y1 = y0 + (Math.cos(a) * g)

      Some(copy(
        version = version + 1,
        start = Vector(x0, y0),
        end = Vector(x1, y1),
        g = g + (d - (d * Math.cos(2 * Math.sqrt((10 / l) * g)))) / 2,
        next = next match {
          case Some(growable) => growable.grow(GrowContext(x1, y1, a))
          case None if version > 10 => Some(Branch(-0.3 * a, Vector(x1, y1), Vector(x1, y1)))
          case _ => None
        },
        children = {
          val grownChildren = children flatMap { _.grow(GrowContext(x1, y1, a)) }

          if(version > 10 && children.isEmpty && Math.random() > 0.99) {
            val childAngle = if(Math.random() > 0.5) Math.PI / 2 else -Math.PI / 2
            Branch(childAngle, Vector(x1, y1), Vector(x1, y1)) :: grownChildren
          } else grownChildren
        }
      ))
    }
  }

}







case class RenderContext(offset: Vector, first: Boolean = false)


object Main {

  def render(ctx: RenderContext, el: Growable) : String = ctx match {
    case RenderContext(offset, isFirst) =>
      el match {
        case Branch(_, start, end, children, next, _, _) => {
          val inner = ((next ++ children) map {
            render(RenderContext(offset), _)
          }).mkString

          (start, end) match {
            case (Vector(x0, y0), Vector(x1,y1)) => {
              if (isFirst)
                s"M${offset.x + x0} ${offset.y - y0} ${inner}L${offset.x + x0} ${offset.y - y0}"
              else
                s"L${offset.x + x1} ${offset.y - y1} ${inner}L${offset.x + x0} ${offset.y - y0} "
            }
          }
        }
      }
  }



  def main() : Unit = {

    val elPath = document.getElementById("treePath").asInstanceOf[svg.Path]

    var tree: Option[Growable] = Some(Branch(0, Vector(0, 0), Vector(0, 0)))

    println("Starting!")

    setInterval(100) {
      tree foreach { g =>
        tree = g.grow(GrowContext(0, 0, 0))
                  .flatMap { _.grow(GrowContext(0, 0, 0)) }
                  .flatMap { _.grow(GrowContext(0, 0, 0)) }
                  .flatMap { _.grow(GrowContext(0, 0, 0)) }

        val pathData = render(RenderContext(Vector(400, 500), first = true), g)

        println(pathData)

        elPath.setAttribute("d", pathData)
      }

    }

  }

}
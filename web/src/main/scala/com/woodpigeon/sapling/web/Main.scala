package com.woodpigeon.sapling.web

import org.scalajs.dom._
import scala.scalajs.js.timers._

case class Vector(dx: Double, dy: Double)

sealed trait Growable
case class Branch(angle: Double, vector: Vector, next: Option[Branch] = None) extends Growable


case class RenderContext(baseX: Double, baseY: Double, baseA: Double, first: Boolean = false)


object Main {

  def render(ctx: RenderContext, el: Growable) : String = ctx match {
    case RenderContext(bX, bY, bA, true) => {
      val inner = render(RenderContext(bX, bY, bA), el)
      s"M $bX $bY $inner Z"
    }
    case RenderContext(bX, bY, bA, _) =>
      el match {
        case Branch(angle, vector, None) => {
          "L 200 200 L 0 500"
        }
      }
  }


  def grow(el: Growable) : Option[Growable] = el match {
    case Branch(angle, vec, None) => Some(el    )
    case Branch(angle, vec, Some(next)) => Some(el)
    case _ => None
  }



  def main() : Unit = {

    val elPath = document.getElementById("treePath").asInstanceOf[svg.Path]

    var tree: Option[Growable] = Some(Branch(0, Vector(10, 10)))

    println("Starting!")

    setInterval(200) {
      tree foreach { g =>
        tree = grow(g)

        val pathData = render(RenderContext(400, 500, 0, first = true), g)
        elPath.setAttribute("d", pathData)
      }

    }

  }

}
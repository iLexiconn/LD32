package org.oc.ld32.gui.editor

import java.util

import org.lengine.maths.Vec2f
import org.lengine.render.{Sprite, TextureRegion, TextureAtlas}
import org.oc.ld32.Game
import org.oc.ld32.gui.GuiScreen
import java.util.{List, ArrayList, Map, HashMap}

import org.oc.ld32.input.gamepad.Controls
import org.oc.ld32.level.{FloorDecoration, Wall}
import org.oc.ld32.render.Animation

import scala.collection.JavaConversions._

class GuiEditor extends GuiScreen {

  val WALL: String = "wall"
  val ENEMY: String = "enemy"
  val FLOOR: String = "floor"

  var currentObject: String = FLOOR

  val options: scala.List[String] = scala.List(ENEMY, FLOOR, WALL)

  val walls: List[Wall] = new ArrayList
  val floorDecorations: List[FloorDecoration] = new ArrayList
  val enemyDefinitions: List[EnemyDef] = new ArrayList
  val anims: Map[String, Animation] = new HashMap
  val nortapSprite = new Sprite("assets/textures/entities/nortap.png", new TextureRegion(0,0,1,1f/4f))
  val floorSprite = new Sprite("assets/textures/gui/editorFloor.png")
  val wallSprite = new Sprite("assets/textures/gui/editorWalls.png")
  val cursorSprite = new Sprite("assets/textures/gui/editorCursor.png")
  var showCursor = false
  var dragging = false
  var startDragX = 0f
  var startDragY = 0f
  val cursor: Vec2f = new Vec2f

  override def init(): Unit = {
    cursor.set(width/2f, height/2f)
    showCursor = false
  }

  def getAnim(id: String): Animation = {
    if(!anims.containsKey(id)) {
      anims.put(id, new Animation(new TextureAtlas(s"assets/textures/entities/$id.png", 16, 16), 5f))
    }
    anims.get(id)
  }

  def updateCursor(delta: Float) = {
    val speed = 5f
    val threshold = 0.10f
    val xAmount = Game.getAxisValue(Controls.moveX)
    val yAmount = Game.getAxisValue(Controls.moveY)
    if(Math.abs(xAmount) > threshold || Math.abs(yAmount) > threshold)
      cursor.set(cursor.x+xAmount*speed,cursor.y-yAmount*speed)

    if(cursor.x < 0) {
      cursor.x = 0
    }
    if(cursor.y < 0) {
      cursor.y = 0
    }
    if(cursor.x > width) {
      cursor.x = width
    }
    if(cursor.y > height) {
      cursor.y = height
    }

    if(dragging) {
      currentObject match {
        case WALL => {
          val wall = walls.remove(0)
          var minX = 0f
          var minY = 0f
          var maxX = 0f
          var maxY = 0f
          if(cursor.x > startDragX) {
            minX = cursor.x
            maxX = startDragX
          } else if(cursor.x <= startDragX) {
            minX = startDragX
            maxX = cursor.x
          }

          if(cursor.y > startDragY) {
            minY = cursor.y
            maxY = startDragY
          } else if(cursor.y <= startDragY) {
            minY = startDragY
            maxY = cursor.y
          }

          val newWall = new Wall("", new Vec2f(minX, minY), new Vec2f(maxX, maxY))
          walls.add(0, newWall)
        }

        case FLOOR => {
          val floor = floorDecorations.remove(0)
          val minX = (Math.min(cursor.x, startDragX)/16f).toInt * 16f
          val minY = (Math.min(cursor.y, startDragY)/16f).toInt * 16f
          val maxX = (Math.max(cursor.x, startDragX)/16f).toInt * 16f
          val maxY = (Math.max(cursor.y, startDragY)/16f).toInt * 16f
          val newFloor = new FloorDecoration(floor.id, minX, minY, maxX-minX, maxY-minY)
          floorDecorations.add(0, newFloor)
        }

        case _ =>
      }
    }
  }

  override def renderScreen(delta: Float): Unit = {
    updateCursor(delta)

    for(floor <- floorDecorations) {
      floor.render(delta)
    }

    for(enemy <- enemyDefinitions) {
      val anim: Animation = getAnim(enemy.id)
      anim.transform.pos.set(enemy.x, enemy.y)
      anim.transform.angle = -(Math.PI/2f).toFloat
      anim.render(delta)
    }

    for(wall <- walls) {
      wall.render(delta)
    }

    nortapSprite.setPos(0,height-nortapSprite.height)
    floorSprite.setPos(64f,height-nortapSprite.height)
    wallSprite.setPos(128f,height-nortapSprite.height)

    wallSprite.render(delta)
    floorSprite.render(delta)
    nortapSprite.render(delta)
    if(showCursor) {
      cursorSprite.setPos(cursor.x - cursorSprite.width/2f, cursor.y - cursorSprite.height/2f)
      cursorSprite.render(delta)
    }
  }

  override def onMousePressed(x: Int, y: Int, button: Int): Unit = {
    super.onMousePressed(x,y,button)
    showCursor = false
    cursor.set(x,y)
    onPressed
  }

  override def onMouseReleased(x: Int, y: Int, button: Int): Unit = {
    super.onMouseReleased(x,y,button)
    showCursor = false
    cursor.set(x,y)
    onReleased
  }

  override def onKeyReleased(keyCode: Int, char: Char): Unit = {
    super.onKeyReleased(keyCode, char)
  }

  def onPressed = {
    if(cursor.y < height-64f) {
      dragging = true
      startDragX = cursor.x
      startDragY = cursor.y
      currentObject match {

        case WALL => {
          val wall = new Wall("", new Vec2f(cursor.x, cursor.y), new Vec2f(cursor.x, cursor.y))
          walls.add(0, wall)
        }

        case FLOOR => {
          val floor = new FloorDecoration("carpet", cursor.x, cursor.y, 16f, 16f)
          floorDecorations.add(0, floor)
        }

        case _ =>
      }
    } else {
      val index = (cursor.x/64f).toInt
      if(index < options.size) {
        currentObject = options(index)
      }
    }
  }

  def onReleased = {
    dragging = false
  }

  override def onKeyPressed(keyCode: Int, char: Char): Unit = {
    super.onKeyPressed(keyCode, char)
  }

  override def onMouseMove(x: Int, y: Int, dx: Int, dy: Int) = {
    super.onMouseMove(x,y,dx,dy)
    showCursor = false
    cursor.set(x,y)
  }

  override def onAxisMoved(value: Float, index: Int) = {
    super.onAxisMoved(value, index)
    showCursor = true
  }

  override def onButtonPressed(button: Int) = {
    super.onButtonPressed(button)
    if(Controls.isConfirmButton(button)) {
      onPressed
    }
  }

  override def onButtonReleased(button: Int) = {
    super.onButtonReleased(button)
    if(Controls.isConfirmButton(button)) {
      onReleased
    }
  }
}

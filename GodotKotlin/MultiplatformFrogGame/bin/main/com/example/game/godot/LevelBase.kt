package com.example.game.godot

import godot.*
import godot.annotation.RegisterClass
import godot.annotation.RegisterFunction
import godot.core.NodePath
import godot.core.Vector2
import godot.global.GD
import kotlin.random.Random

@RegisterClass
class LevelBase: Node2D() {
	val CELL_SIZE = 16
	val SPAWN_TIME = 0.5
	val MOVE_TIME = 0.5
	val TURNS_FOR_SPAWN = 2

	val SIZE_H = 20
	val SIZE_V = 10

	var hTiles = 1
	var vTiles = 1

	var totalTurns = 0

	lateinit var frogPackedScene: PackedScene
	lateinit var carPackedScene: PackedScene

	lateinit var tilemap: TileMap
	lateinit var carContainer: Node2D

	@RegisterFunction
	override fun _ready() {
		frogPackedScene = ResourceLoader.load("res://scenes/Frog.tscn") as PackedScene
		carPackedScene = ResourceLoader.load("res://scenes/Car.tscn") as PackedScene

		tilemap = getNode(NodePath("TileMap")) as TileMap
		carContainer = getNode(NodePath("CarContainer")) as Node2D

		initialize(SIZE_H, SIZE_V)

		var viewport = getParent() as Viewport
		viewport.size = Vector2(CELL_SIZE * hTiles, CELL_SIZE * vTiles)
	}

	@RegisterFunction
	fun initialize(hSize: Int, vSize: Int) {
		var tHSize = hSize
		var tVSize = vSize

		if (hSize < 5) {
			tHSize = 5
		}
		if (vSize < 5) {
			tVSize = 5
		}

		tilemap.clear()

		for (i in 0 until tHSize) {
			for (j in 0 until tVSize) {
				if (j == 0 || j == tVSize - 1) {
					tilemap.setCellv(Vector2(i, j), 0)
				} else if (j == 1) {
					tilemap.setCellv(Vector2(i, j), 1)
				} else if (j == tVSize - 2) {
					tilemap.setCellv(Vector2(i, j), 3)
				} else {
					tilemap.setCellv(Vector2(i, j), 2)
				}
			}
		}

		var frog = frogPackedScene.instance() as Frog
		frog.position = Vector2(CELL_SIZE/2.0, (vSize-1)*CELL_SIZE + CELL_SIZE/2.0)
		addChild(frog)

		hTiles = tHSize
		vTiles = tVSize

	}

	@RegisterFunction
	fun isInMap(pos: Vector2): Boolean {
		if (pos[1] == 0.0) {
			reset()
		}

		return (0 <= pos[0] && pos[0] < hTiles) && (0 <= pos[1] && pos[1] < vTiles)
	}

	@RegisterFunction
	fun reset() {
		getTree()?.reloadCurrentScene()
	}

	@RegisterFunction
	fun turn() {
		totalTurns += 1
		if (totalTurns >= TURNS_FOR_SPAWN) {
			totalTurns = 0
			spawn()
		}
	}

	@RegisterFunction
	fun spawn() {
		var car = carPackedScene.instance() as Car
		car.position = Vector2(Random.nextInt(0, 1) * (hTiles-1) * CELL_SIZE + CELL_SIZE/2.0, Random.nextInt(1, vTiles - 2) * CELL_SIZE + CELL_SIZE/2.0)
		addChild(car)
		car.callDeferred("initialize", Vector2(1, 0), MOVE_TIME)
	}
}

///|
test "move_player moves within bounds by the given delta" {
  assert_eq(move_player(50, 5, 0, 100), 55)
}

///|
test "move_player clamps at the upper bound" {
  assert_eq(move_player(98, 10, 0, 100), 100)
}

///|
test "move_player clamps at the lower bound" {
  assert_eq(move_player(2, -10, 0, 100), 0)
}

///|
test "fall_step advances y by the given speed" {
  assert_eq(fall_step(10, 3), 13)
}

///|
test "is_collision detects overlapping player and block" {
  assert_eq(is_collision(10, 20, 15, 10, 100, 100, 0), 1)
}

///|
test "is_collision reports no overlap when horizontally separated" {
  assert_eq(is_collision(10, 20, 100, 10, 100, 100, 0), 0)
}

///|
test "is_collision reports no overlap when vertically separated beyond margin" {
  assert_eq(is_collision(10, 20, 15, 10, 100, 50, 5), 0)
}

///|
test "is_collision respects the hit margin when checking vertical distance" {
  assert_eq(is_collision(10, 20, 15, 10, 100, 95, 5), 1)
}

///|
test "is_collision detects overlap when the block is below the player within margin" {
  assert_eq(is_collision(10, 20, 15, 10, 95, 100, 5), 1)
}

///|
test "is_off_screen reports true once a block passes the screen height" {
  assert_eq(is_off_screen(650, 600), 1)
}

///|
test "is_off_screen reports false while a block is still within the screen" {
  assert_eq(is_off_screen(600, 600), 0)
}

///|
test "should_spawn fires on every tick that is a multiple of the interval" {
  assert_eq(should_spawn(30, 30), 1)
}

///|
test "should_spawn stays silent between intervals" {
  assert_eq(should_spawn(31, 30), 0)
}

///|
test "should_spawn fires on tick zero" {
  assert_eq(should_spawn(0, 30), 1)
}

///|
test "increment_score adds one to the current score" {
  assert_eq(increment_score(9), 10)
}

///|
test "spawn_block returns an x position within the requested range" {
  let (x, _next_seed) = spawn_block(1, 0, 9)
  assert_true(x >= 0 && x <= 9)
}

///|
test "spawn_block is deterministic for a given seed" {
  assert_eq(spawn_block(42, 0, 100), spawn_block(42, 0, 100))
}

///|
test "spawn_block advances the seed" {
  let (_x, next_seed) = spawn_block(7, 0, 9)
  assert_true(next_seed != 7)
}

///|
test "spawn_block produces a different x for a different seed" {
  let (x1, _) = spawn_block(1, 0, 1000)
  let (x2, _) = spawn_block(2, 0, 1000)
  assert_true(x1 != x2)
}

///|
#export_name("move_player")
pub fn move_player(x : Int, dx : Int, min_x : Int, max_x : Int) -> Int {
  let moved = x + dx
  if moved < min_x {
    min_x
  } else if moved > max_x {
    max_x
  } else {
    moved
  }
}

///|
#export_name("fall_step")
pub fn fall_step(y : Int, speed : Int) -> Int {
  y + speed
}

///|
#export_name("is_collision")
pub fn is_collision(
  player_x : Int,
  player_width : Int,
  block_x : Int,
  block_size : Int,
  player_y : Int,
  block_y : Int,
  hit_margin : Int,
) -> Int {
  let horizontal_overlap = player_x < block_x + block_size &&
    block_x < player_x + player_width
  let vertical_distance = if block_y > player_y {
    block_y - player_y
  } else {
    player_y - block_y
  }
  if horizontal_overlap && vertical_distance <= hit_margin {
    1
  } else {
    0
  }
}

///|
#export_name("is_off_screen")
pub fn is_off_screen(block_y : Int, screen_height : Int) -> Int {
  if block_y > screen_height {
    1
  } else {
    0
  }
}

///|
#export_name("should_spawn")
pub fn should_spawn(tick_count : Int, interval : Int) -> Int {
  if tick_count % interval == 0 {
    1
  } else {
    0
  }
}

///|
#export_name("increment_score")
pub fn increment_score(score : Int) -> Int {
  score + 1
}

///|
fn advance_seed(seed : Int) -> Int {
  (seed * 1103515245 + 12345) & 0x7fffffff
}

///|
#export_name("spawn_block")
pub fn spawn_block(seed : Int, min_x : Int, max_x : Int) -> (Int, Int) {
  let next_seed = advance_seed(seed)
  let range = max_x - min_x + 1
  let x = min_x + next_seed % range
  (x, next_seed)
}

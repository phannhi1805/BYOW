# BYOW DESIGN DOCUMENT
**Name:** Nhi Phan, Richard Lee

##Classes and Data Structure

###Room

* WIDTH and HEIGHT: dimensions of the world
* RANDOM: create a random number form seed
* roomWidth and roomHeight: generate room width and height within range [3,9). Min 3 is because a room need at least 2 walls and 1 floor tile on each row/column







##Algorithms

* shift(dx, dy): Go from one position to another dx and dy from the current position
* fillBoardWithNothing: Generate world of nothing tiles
* drawRow and drawCol: Put down tiles in rows or columns
* makeRoomFloor: draw each row of floor until the height is 0
* makeRoomWall: draw the wall outlining the floor tiles


##Persistence
<div class="mermaid">
     graph TD
      CWD --- .gitlet
      .gitlet --- Staging-Area
.gitlet --- Commit-Folder
.gitlet --- HEAD
Staging-Area --- Staging-for-addition
Staging-Area --- Staging-for-removal
</div>


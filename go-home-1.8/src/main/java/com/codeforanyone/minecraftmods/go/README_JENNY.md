Go-Home Mod Features
----------------------
The `/go home` command teleports you to world-spawn as long as it's safe to land.  Safe: Solid 
block to stand on, (optionally water at your feet, but solid beneath,) air to breathe, air 
above your head, and not standing in fire or lava.  No blocks intersecting the player's 
body when they land (except water), and room to jump up one block.  They might be unhappily buried inside 
a hill in the dark, but they won't hit an insta-death unless a complex intentional trap is present.

Since the home location is exactly world spawn, you can lock this region on a multi-player server so
that other players can't set traps.  Note that when you enter a world the first time, you spawn within
a many-block radius near, but not exactly at, world spawn.  It may be worthwhile to use a world
editor to move the world spawn somewhere useful and more easily accessed, then protect it, and then
build a base nearby for newly hatched players.

The `/go add`, `/go rm`, `/go add-global`, and `/go rm-global` are used to 
save named locations (besides "home" which is reserved).  Global locations work 
cross-dimensionally, while non-global ones only work within the same dimension.
Right now, only operators are able to add/remove named locations on multi-player servers, but 
all players can use them once added.  Any player can add locations on a single-player 
server (I have no idea how this behaves after open-to-lan is activated).

Last Build: Nov 21, 2015 against Forge 1.8-11.14.4.1572-mdk


Getting Started After a Forge Update
-------------------------------------

1. Download the latest MDK (mod dev kit) or source build from Forge.
2. Unzip it somewhere useful within the workspace.
3. Migrate the src/main/* folders in, using git or whatever.
4. Make sure everything compiles.
5. Then build and test.

This git repository intentionally does not include the forge bits as they're easily extracted and inconvenient to
keep up to date via git.


Building
--------
`./gradlew build`

This produces a usable mod zip inside /build/libs/ which you can distribute.


Testing
-------
`./gradlew runClient`


Regression Test Cases
----------------------

### Testing go home:

1. World is created okay.
2. Mod registers itself with Forge.
3. Type `/go` - you should see a list of options such as home.
4. Type `/go home` - you should see either a successful teleport or a meaningful unsafe-location warning and coordinates.
5. Go to world spawn and dig it out if it's buried in a hill or something stupid like that.
6. Type `/go home` - you should be able to work towards a safe landing and a successful teleport.
7. Use `/go home` from the overworld successfully.
8. Use `/go home` from the nether successfully and see it change to the overworld.
9. Make the spawn location unsafe.  
10. Try `/go home` from the overworld and see it blocked.
11. Try `/go home` from the nether and see it blocked.
12. Spawn in a horse and tame it and saddle it.
13. Try `/go home` while mounted.  You should successfully unmount and then teleport.

### Testing add + rm (rm/remove/del/delete aliases):

1. Type `/go` to list the currently known named locations.  You will do this repeatedly as you add and remove some.
2. Walk somewhere interesting. Type `/go add testbase` to add a new location where you're standing.
3. Type `/go` to list the named locations.  Was it properly added?
4. Walk somewhere else.  Type `/go testbase` to see if it takes you back to the right spot.
5. This was a same-dimension name.  Go to the nether and type `/go` - it should not be listed as an option.  
6. Try typing `/go testbase` anyway; it should give a not-found message.
7. Go back to the overworld (hint: `/go home`).  Type `/go` and see that the name is listed again.
8. Type `/go rm testbase` and then `/go` to check that it was properly deleted.  Try `/go testbase` to confirm it's really gone.

### Testing add-global + rm-global:

1. Type `/go` to list the currently known named locations.  You will do this repeatedly as you add and remove some.
2. Walk somewhere interesting. Type `/go add-global testbase` to add a new location where you're standing.
3. Type `/go` to list the named locations.  Was it properly added?
4. Walk somewhere else.  Type `/go testbase` to see if it takes you back to the right spot.
5. This was an any-dimension name.  Go to the nether and type `/go` - it should be listed as an option.  
6. Try typing `/go testbase` and it should cross-dimensional teleport you to the right location.
7. Type `/go rm-global testbase` and then `/go` to check that it was properly deleted.  Try `/go testbase` to confirm it's really gone.
8. Go to the nether.  Create a named location there `/go add-global nether`  Go to the overworld.  Use `/go nether` and confirm it works.
9. Type `/go rm-global nether`


### Author Info

<https://github.com/jennybrown8>

jennybrown8 @ gmail.com

This is not a high priority project for me, so while bug reports are welcome, they might be a long time in the fixing.






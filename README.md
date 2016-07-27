IV Tool
=======

Syntax
------

```
ivtool [-token <token>] [-ptc -user <user> -pass <pass>] [-r] [-f] [-sleep <x>]
 -f,	--force 		forces the rename for pokemon that already have a nickname
 -h,	--help  		print this message
		--pass <arg>	your ptc password
		--ptc   		use ptc for login instead of google
 -r,	--rename		rename every pokemon without a nickname
		--sleep <arg>   the time the tool waits after each renaming (default = 1000ms)
		--token <arg>   your google token if you have one (not needed)
		--user <arg>	your ptc username
```

Examples
--------

1) You want to list all your pokemon and use a google account: 
```
java -jar ivtool.jar
```
2) You want to rename all your pokemon and use a google account: 
```
java -jar ivtool.jar -r -f
```
3) You want to rename all your pokemon and use a ptc account:
```
java -jar ivtool.jar -r -f -ptc -user=admin -pass=1234
```
Example output
--------------

```
41 ZUBAT 33% 9 0 6 120CP 
44 GLOOM 38% 3 14 0 333CP 
46 PARAS 78% 13 11 11 397CP 
48 VENONAT 31% 14 0 0 414CP 
52 MEOWTH 71% 13 11 8 115CP 
52 MEOWTH 64% 13 9 7 51CP 
54 PSYDUCK 82% 11 13 13 424CP 
56 MANKEY 56% 13 0 12 59CP 
61 POLIWHIRL 71% 13 8 11 171CP 
61 POLIWHIRL 64% 4 14 11 17CP 
```

Changelog
---------

0.0.2

* sort output
* wait after each renaming (-sleep <time in ms>)
* option to force the renaming (-f | -force)

0.0.1

* initial release

Wishlist
--------

* star pokemon with iv >= x% (currently not possible)
* transfer duplicate pokemon with iv <= x%

Credits
-------

* Java API (https://github.com/Grover-c13/PokeGOAPI-Java)

/!\ WARNING /!\
===============

This tool uses inofficial API calls to gather the needed information, so there is a possibility to be banned for using it. 
I'm not responsible for any damage to your account. Use the tool at your own risk.

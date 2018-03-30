<p align="center">
  <img src="https://unascribed.com/foamfix/logo.png">
</p>

<p align="center"><a href="https://asie.pl/files/mods/FoamFix/">Build archive</a> | <a href="https://unascribed.com/foamfix/">Unascribed's unofficial builds</a></p>



FAQ
===
**What is FoamFix?**   
FoamFix is a mod designed to optimize post-1.7.10 modded Minecraft using simple, targeted optimizations. As of 0.6.1, observations show that Java heap usage (measured using VisualVM on the main menu) can drop by as much as 50%!   

**How does FoamFix achieve this?**   
Unascribed posted a detailed write-up, including what each config option does, which can be [found here.](https://unascribed.com/b/2017-17-10-so-heres-how-foamfix-works.html)   

**What's the difference between the Lawful and Anarchy Editions?**   
The Anarchy Edition utilizes a coremod, while the Lawful Edition does not. Generally, this means that the latter is less invasive; however, in most cases, it's recommended to use the Anarchy edition.   

**Can I run FoamFix on a client if it's not on the server (and vice versa)?**   
Yes.

**Have you got any scientifically accurate graphs?**   
Sure.   
![Completely accurate graph](https://cdn.tropicraft.net/foamfix.png)   



Additional Tips
===
- Enable alwaysSetupTerrainOffThread in forge.cfg. (FoamFix enables this by default on new instances)
- Install [BetterFPS](https://minecraft.curseforge.com/projects/betterfps) - it optimizes other areas of Minecraft not targeted by FoamFix.
- If you're experiencing "ghost chunk loading"-related lag (a noticeable lag spike every ~30 seconds), a way to partially work around the problem is to increase the value of dormantChunkCacheSize in forgeChunkLoading.cfg.
- For some GPU/driver configurations, disabling mipmaps (setting Mipmap Levels to 0) will boost FPS considerably. (FoamFix enables this by default on new instances)
- On dedicated servers, setting max-tick-time=0 can solve crashes related to "java.lang.Error: Watching server". Still looking into why this happens!



Reporting Bugs
===
When reporting any bugs, please follow these steps before submitting an issue:
1) Try to reproduce it **without** FoamFix. If it's still present without FoamFix, it is most likely an issue with another mod.
2) Check to see if your issue has already been reported.

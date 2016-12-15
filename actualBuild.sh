#!/bin/sh
gradle build
mkdir tmp
cd tmp
rm -r *
unzip ../build/libs/foamfix-$1.jar
rm -rf chan #old stuff
mv META-INF/MANIFEST.MF NOT_MANIFEST.MF
cat <<EOF >META-INF/MANIFEST.MF
Manifest-Version: 1.0
FMLCorePluginContainsFMLMod: true
FMLCorePlugin: pl.asie.foamfix.coremod.FoamFixCore
EOF
jar cvfm ../foamfix-$1-anarchy.jar META-INF/MANIFEST.MF pl
rm META-INF/MANIFEST.MF
mv NOT_MANIFEST.MF META-INF/MANIFEST.MF
rm -rf pl/asie/foamfix/coremod
jar cvfm ../foamfix-$1-law.jar META-INF/MANIFEST.MF pl

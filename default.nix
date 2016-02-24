{ pkgs ? import <nixpkgs> {} , execCommand ? "" }:

with pkgs;
with lib;

let
  buildsbt = map (splitString " := ") (splitString "\n\n" (builtins.readFile ./build.sbt));
  version = builtins.replaceStrings ["\""] [""] (last (findFirst (p: (builtins.head p) == "version") "" buildsbt));
  sbtVersion = last (splitString "=" (builtins.readFile ./project/build.properties));
  sbt = fetchurl {
    url = "http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/${sbtVersion}/sbt-launch.jar";
    sha256 = "04k411gcrq35ayd2xj79bcshczslyqkicwvhkf07hkyr4j3blxda";
  };
in
stdenv.mkDerivation rec {
   name = "scsh-${version}";

   src = ./.;

   patchPhase = ''
     sed -i 's,\$assembly-jar\$,nix-output/jars/scsh.jar,g' src/main/shell/scsh
     ${if ((stringLength execCommand) == 0)
       then "sed -i 's,\"java\",\"${pkgs.jdk}/bin/java\",g' build.sbt"
       else "sed -i 's,\"java\",\"${execCommand}\",g' build.sbt"}
   '';

   buildPhase = ''
     mkdir _sbt
     export SBT_OPTS="-Dsbt.boot.directory=_sbt/boot/ -Dsbt.ivy.home=_sbt/ivy2/ -Dsbt.global.base=_sbt/"
     ${pkgs.jdk}/bin/java $SBT_OPTS -jar ${sbt} gen-scsh
   '';

   installPhase = ''
     mkdir -p $out/{jars,bin}
     cp target/scala-2.11/scsh-assembly-${version}.jar $out/jars/scsh.jar
     cp target/bin/scsh $out/bin/scsh
     sed -i "s,nix-output,$out,g" $out/bin/scsh
   '';
}

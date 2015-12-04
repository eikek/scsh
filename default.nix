{ stdenv, fetchgit, sbt, jdk, execCommand ? "" }:
with builtins;
with stdenv.lib;
stdenv.mkDerivation rec {
   version = "0.1.1";

   name = "scsh-${version}";

   src = fetchgit {
     url = "https://github.com/eikek/scsh.git";
     rev = "refs/heads/master";
     sha256 = "11ramf5gpb2cdzsrrnsipgkf4wdj5qynwc5rk3slqvxsc1aprlfw";
   };

   patchPhase = ''
     sed -i 's,\$assembly-jar\$,nix-output/jars/scsh.jar,g' src/main/shell/scsh
     ${if ((stringLength execCommand) == 0)
       then "sed -i 's,\"java\",\"${jdk}/bin/java\",g' build.sbt"
       else "sed -i 's,\"java\",\"${execCommand}\",g' build.sbt"}
   '';

   buildPhase = ''
     mkdir _sbt
     export SBT_OPTS="-Dsbt.boot.directory=_sbt/boot/ -Dsbt.ivy.home=_sbt/ivy2/ -Dsbt.global.base=_sbt/"
     ${sbt}/bin/sbt gen-scsh
   '';

   installPhase = ''
     mkdir -p $out/{jars,bin}
     cp target/scala-2.11/scsh-assembly-${version}.jar $out/jars/scsh.jar
     cp target/bin/scsh $out/bin/scsh
     sed -i "s,nix-output,$out,g" $out/bin/scsh
   '';
}

with (import <nixpkgs> {});
import ./default.nix {
  inherit stdenv fetchgit sbt jdk;
#  execCommand = "${drip}/bin/drip";
}

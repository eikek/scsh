with (import <nixpkgs> {});
import ./default.nix {
  execCommand = "${drip}/bin/drip";
}

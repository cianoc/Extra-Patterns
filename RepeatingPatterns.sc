
PperiodFn : Pfunc {
	*new {|fn|
    ^super.new({|event|
      var beatFrac = event[\beat].frac ? thisThread.beats.frac;
      fn.value(beatFrac*2*pi)});
	}
}
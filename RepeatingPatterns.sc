
Pperfn : Pfunc {
	*new {|fn|
		^super.new({fn.value(thisThread.beats.frac*2*pi)});
	}
}
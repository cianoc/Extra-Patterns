Pclip : Pattern {
	var pbind;

	*new {|... pairs|
		^super.new.init(pairs);
	}

	init{|... pairs|
		pbind = Pbind(pairs);
	}

	patternpairs{
		^pbind.patternpairs;
	}

	storeArgs { ^pbind.patternpairs }

	embedInStream{|inevent|
		var startTime = thisThread.beats;
		var currTime = startTime;
		var stream = pbind.asStream;

		loop{
			var t;
			var evt = stream.next(inevent);
			if (evt.isNil) { ^nil.yield };

			if ((t = evt[\bar]).notNil){
				var bars = t.floor;

			}

			if(event[\bar].isNil.not  ||
				event[\beat].isNil.not  ||
				event[\barDur].isNil.not)
			{
				event = inevent.copy;

			next = stream.next(event);
			next.notNil;
		}{
			if(

	}
}

Pkr : Pfunc {
	*new {|bus|
		var check;
		var last = 0.0;

		bus = bus.asBus;

		// audio?
		bus.isSettable.not.if {
			"Not a kr Bus or NodeProxy. This will only yield 0".warn;
			^Pfunc({0});
		};

		check = {bus.server.hasShmInterface}.try;

		check.if ({
			^Pfunc({bus.getSynchronous()});
		}, {
			"No shared memory interface detected. Use localhost server on SC 3.5 or higher to get better performance".warn;
			bus.get({|v| last = v;});
			^Pfunc({bus.get({|v| last = v;}); last;});
		});
	}
}

/*
	Plast
	(c) 2010 by Patrick Borgeat <patrick@borgeat.de>
	http://www.cappel-nord.de

	Part of BenoitLib
	http://github.com/cappelnord/BenoitLib
	http://www.the-mandelbrots.de

	Remembers the last value of a key.

*/

Plast : Pattern {

	var <>key;
	var last;

	*new {|key|
		^super.new.key_(key);
	}

	embedInStream {|event|
		var ret;

		while {true} {
			ret = last ?? {event.use({event.at(key).value})};
			last = event.use({event.at(key).value});
			last.yield;
		};

		^event;
	}
}

/*
Can take either an array of format: [[beat, event], [beat, event]...]

Or a priorityqueue containing events. Time is assume to start at 0, so if the first beat is larger a rest will be inserted.

/delta add to the events when they're pushed out as a

*/
Pscore : Pattern {
	var <score, <repeats;
	*new{|score, repeats=1|
		^super.new().init(score, repeats);
	}
	init{|sco, rep|
		var beat, evt;
		var currBeat = 0;
		var list = List(sco.size());
		repeats = rep;

		sco.do{|act, i|
			var nxtEvent = act[1].copy;

			if(act[0] > currBeat){
				var len = act[0] - currBeat;

				Pscore.pr_addEvent(list, (isRest: true, dur: len, delta: len));
				currBeat = currBeat + len;
			};

			if(sco[i+1].isNil.not){
				var d = sco[i+1][0] - act[0];
				if (d <= nxtEvent.delta){
					nxtEvent[\delta] = sco[i+1][0] - act[0];
				}
			};
			if(nxtEvent[\delta].isNil){
				nxtEvent[\delta] = nxtEvent[\dur] * (nxtEvent[\stretch] ? 1);
			};
			Pscore.pr_addEvent(list, nxtEvent);
			currBeat = currBeat + nxtEvent.delta;
		};

		score = list.asArray;
	}

	*pr_addEvent{|list, event|
		if(event.isRest && list.last.isNil.not && list.last.isRest){
			var dur = event[\dur] + list.last[\dur];
			var delta = event[\delta] + list.last[\delta];
			list.last[\dur] = dur;
			list.last[\delta] = delta;
		}{
			list.add(event);
		}
	}

	embedInStream { arg inval;
		var dif = 0;

		repeats.do{
			score.do{|evt|
				inval = evt.embedInStream(inval);
			}
		};

		^inval;
	}

	storeArgs { ^[score] }
}


/* Returns beat in bar */
Pbeat : Pattern {
	var <>repeats;
	*new { arg repeats=inf;
		^super.newCopyArgs(repeats)
	}
	storeArgs { ^[repeats] }
	embedInStream { arg inval;
		var start = thisThread.beats;
		repeats.value(inval).do { inval = (thisThread.beatInBar).yield };
		^inval
	}
}

Pdrum {
	*new{|sequence, grid=(1/16), tuplet=2, repeats=inf|
		^Pbind(
			\dur, grid * (2/tuplet),
			\amp, Pseq(sequence, repeats),
			\type, Pif(Pkey(\amp) > 0, \note, \rest)
		)
	}
}

Pbindrum {
	*new{|key, sequence, grid=(1/16), tuplet=2, repeats=1|
		^Pbindef(key,
			\dur, grid * (2/tuplet),
			\amp, Pseq(sequence, repeats),
			\type, Pif(Pkey(\amp) > 0, \note, \rest)
		)
	}
}

Ptab {
	*new{|tab, grid=(1/16), tuplet=2, repeats=inf|
		var func = {
			tab.do{|beat|
				if(beat == $x){
					true.yield
				};
				if(beat == $o || beat == $_){
					false.yield;
				}
		}};

		^Pbind(\dur, grid, \type, Pn(Pif(Prout(func), \note, \rest), repeats));
	}
}

Pbindtab {
	*new{|key, tab, grid=(1/16), tuplet=2, repeats=inf|
		var func = {
			tab.do{|beat|
				if(beat == $x){
					true.yield
				};
				if(beat == $o || beat == $_){
					false.yield;
				}
		}};

		^Pbindef(key, \dur, grid, \type, Pn(Pif(Prout(func), \note, \rest), repeats));
	}
}
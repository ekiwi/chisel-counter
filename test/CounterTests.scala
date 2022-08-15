// Copyright 2022 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

import org.scalatest.freespec.AnyFreeSpec
import chiseltest._

class CounterTests extends AnyFreeSpec with ChiselScalatestTester {
  "unit test counting" in {
    test(new Counter).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      // enable default value
      dut.io.enable.poke(true)
      // initial expected outputs
      dut.io.overflow.expect(false)
      dut.io.count.expect(0)
      // after one step, the count should be one more
      dut.clock.step()
      dut.io.count.expect(1)
      // 11 steps with enable=true
      dut.clock.step(10)
      dut.io.count.expect(11)
      // disable for one cycle
      dut.io.enable.poke(false)
      dut.clock.step()
      dut.io.count.expect(11)
      dut.io.overflow.expect(false)
      // step up to max value
      dut.io.enable.poke(true)
      dut.clock.step(4)
      // check that the value is correct
      dut.io.count.expect(15)
      // no overflow yet
      dut.io.overflow.expect(false)
      // take step with counter disabled
      dut.io.enable.poke(false)
      dut.clock.step()
      // the count does not change, but the expected value does
      dut.io.count.expect(15)
      dut.io.overflow.expect(true)
    }
  }

  "random testing" in {
    val seeds = Seq(1,2,3)
    val cycles = 100
    seeds.foreach { seed =>
      var count = 0
      var overflow = false
      def next(enabled: Boolean): Unit = {
        if(count == 15) overflow = true
        if(enabled) count += 1
        if(count == 16) count = 0
      }
      val rnd = new scala.util.Random(seed)
      test(new Counter) { dut =>
        (0 until cycles).foreach { _ =>
          val enabled = rnd.nextBoolean()
          dut.io.enable.poke(enabled)
          dut.io.count.expect(count)
          dut.io.overflow.expect(overflow)
          dut.clock.step()
          next(enabled)
        }
      }
    }
  }

}

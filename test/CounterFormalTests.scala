// Copyright 2022 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

import org.scalatest.freespec.AnyFreeSpec
import chisel3._
import chiseltest._
import chiseltest.formal._



class CounterFormalTests extends AnyFreeSpec with ChiselScalatestTester with Formal {
  "check some fundamental properties" in {
    verify(new Module {
      val dut = Module(new Counter) ; val io = IO(chiselTypeOf(dut.io)) ; io <> dut.io

      // increment when enabled
      when(past(dut.io.enable)) {
        chisel3.assert(dut.io.count === (past(dut.io.count) + 1.U))
      } .otherwise {
        chisel3.assert(stable(dut.io.count))
      }

      // mark overflow after one cycle
      when(past(dut.io.count === 15.U)) {
        chisel3.assert(dut.io.overflow)
      }

      // overflow is never revoked
      when(past(dut.io.overflow)) {
        chisel3.assert(dut.io.overflow)
      }
    }, Seq(BoundedCheck(30), BtormcEngineAnnotation))
  }

  "check against a golden model" in {
    verify(new Module {
      val dut = Module(new Counter) ; val io = IO(chiselTypeOf(dut.io)) ; io <> dut.io

      // golden model
      val count = RegInit(0.U(32.W))
      val overflow = RegInit(false.B)
      when(count === 15.U) { overflow := true.B }
      when(dut.io.enable) { count := count + 1.U }
      when(dut.io.enable && count === 15.U) { count := 0.U }

      // check equivalence
      chisel3.assert(dut.io.count === count)
      chisel3.assert(dut.io.overflow === overflow)
    }, Seq(BoundedCheck(30), BtormcEngineAnnotation))
  }

}

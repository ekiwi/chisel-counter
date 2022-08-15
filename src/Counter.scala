// Copyright 2022 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

import chisel3._


class CounterIO extends Bundle {
  val enable = Input(Bool())
  val count = Output(UInt(4.W))
  val overflow= Output(Bool())
}

class Counter extends Module {
  val io = IO(new CounterIO)

  val count = RegInit(0.U(4.W))
  val overflow = RegInit(false.B)
  when(io.enable) {
    count := count + 1.U
  }
  when(count === "b1111".U) {
    overflow := true.B
  }

  io.count := count
  io.overflow := overflow
}


object Generator extends App {
  println(getVerilogString(new Counter))
}
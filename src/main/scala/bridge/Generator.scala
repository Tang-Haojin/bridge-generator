package bridge

import circt.stage.ChiselStage
import org.chipsalliance.cde.config.Config
import freechips.rocketchip.diplomacy._

object Main extends App {
  val mod = DisableMonitors(p => LazyModule(new TL2AXI4()(p)))(new Config((site, here, up) => PartialFunction.empty))
  ChiselStage.emitSystemVerilogFile(
    mod.module,
    Array("-td", "./build/rtl", "--dump-fir", "--target", "verilog"),
    Array("-O=release", "--disable-annotation-unknown", "--lowering-options=explicitBitcast,disallowLocalVariables,disallowPortDeclSharing,locationInfoStyle=none")
  )
}

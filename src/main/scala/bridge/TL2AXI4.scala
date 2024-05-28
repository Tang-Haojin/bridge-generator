package bridge

import chisel3._
import chisel3.util._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.tilelink._

class TL2AXI4
(
  address: Seq[AddressSet] = Seq(AddressSet(0x0, 0xffffffffL)),
  executable: Boolean = false,
  beatBytes: Int = 4,
  burstLen: Int = 1
)(implicit p: Parameters) extends SimpleLazyModule {
  val axi4node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    Seq(AXI4SlaveParameters(
      address,
      regionType = RegionType.UNCACHED,
      executable = executable,
      supportsWrite = TransferSizes(1, beatBytes * burstLen),
      supportsRead = TransferSizes(1, beatBytes * burstLen),
      interleavedId = Some(0)
    )),
    beatBytes = beatBytes
  )))
  val tlnode = TLClientNode(Seq(TLMasterPortParameters.v1(
    clients = Seq(TLMasterParameters.v1(
      "tl",
      sourceId = IdRange(0, 1)
    ))
  )))
  axi4node :=
    AXI4IdIndexer(1) :=
    AXI4Buffer() :=
    AXI4Buffer() :=
    AXI4UserYanker(Some(1)) :=
    TLToAXI4() :=
    TLWidthWidget(beatBytes) :=
    TLFIFOFixer() :=
    tlnode

  val axi4 = InModuleBody {
    axi4node.makeIOs()
  }

  val tl = InModuleBody {
    tlnode.makeIOs()
  }
}

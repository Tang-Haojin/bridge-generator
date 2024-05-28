import mill._
import scalalib._
import $file.`rocket-chip`.common
import $file.`rocket-chip`.cde.common
import $file.`rocket-chip`.hardfloat.build

val defaultScalaVersion = "2.13.10"

def defaultVersions(chiselVersion: String) = chiselVersion match {
  case "chisel" => Map(
    "chisel"        -> ivy"org.chipsalliance::chisel:6.3.0",
    "chisel-plugin" -> ivy"org.chipsalliance:::chisel-plugin:6.3.0",
    "chiseltest"    -> ivy"edu.berkeley.cs::chiseltest:5.0.2"
  )
}

trait HasChisel extends SbtModule with Cross.Module[String] {
  def chiselModule: Option[ScalaModule] = None

  def chiselPluginJar: T[Option[PathRef]] = None

  def chiselIvy: Option[Dep] = Some(defaultVersions(crossValue)("chisel"))

  def chiselPluginIvy: Option[Dep] = Some(defaultVersions(crossValue)("chisel-plugin"))

  override def scalaVersion = defaultScalaVersion

  override def scalacOptions = super.scalacOptions() ++
    Agg("-language:reflectiveCalls", "-Ymacro-annotations", "-Ytasty-reader")

  override def ivyDeps = super.ivyDeps() ++ Agg(chiselIvy.get)

  override def scalacPluginIvyDeps = super.scalacPluginIvyDeps() ++ Agg(chiselPluginIvy.get)
}

object rocketchip extends Cross[RocketChip]("chisel")

trait RocketChip
  extends millbuild.`rocket-chip`.common.RocketChipModule
    with HasChisel {
  def scalaVersion: T[String] = T(defaultScalaVersion)

  override def millSourcePath = os.pwd / "rocket-chip"

  def macrosModule = macros

  def hardfloatModule = hardfloat(crossValue)

  def cdeModule = cde

  def mainargsIvy = ivy"com.lihaoyi::mainargs:0.5.4"

  def json4sJacksonIvy = ivy"org.json4s::json4s-jackson:4.0.6"

  object macros extends Macros

  trait Macros
    extends millbuild.`rocket-chip`.common.MacrosModule
      with SbtModule {

    def scalaVersion: T[String] = T(defaultScalaVersion)

    def scalaReflectIvy = ivy"org.scala-lang:scala-reflect:${defaultScalaVersion}"
  }

  object hardfloat extends Cross[Hardfloat](crossValue)

  trait Hardfloat
    extends millbuild.`rocket-chip`.hardfloat.common.HardfloatModule with HasChisel {

    def scalaVersion: T[String] = T(defaultScalaVersion)

    override def millSourcePath = os.pwd / "rocket-chip" / "hardfloat" / "hardfloat"

  }

  object cde extends CDE

  trait CDE extends millbuild.`rocket-chip`.cde.common.CDEModule with ScalaModule {

    def scalaVersion: T[String] = T(defaultScalaVersion)

    override def millSourcePath = os.pwd / "rocket-chip" / "cde" / "cde"
  }
}

// extends this trait to use XiangShan in other projects
trait BridgeModule extends ScalaModule {

  def rocketModule: ScalaModule

  override def moduleDeps = super.moduleDeps ++ Seq(
    rocketModule,
  )
}

object bridge extends Cross[Bridge]("chisel")
trait Bridge extends BridgeModule with HasChisel {

  override def millSourcePath = os.pwd

  def rocketModule = rocketchip(crossValue)

  override def forkArgs = Seq("-Xmx40G", "-Xss256m")
}

package sryza

import de.sciss.synth.Server

import de.sciss.synth._
import de.sciss.synth._
import ugen.{EnvGen, _}
import Ops._


object SoundPlayboc {
  def main(args: Array[String]): Unit = {
    val cfg = Server.Config()
    cfg.program = "/Applications/SuperCollider/SuperCollider.app/Contents/Resources/scsynth"
    Server.run(cfg) { s =>


      val x = play {
        val z = RLPF.ar(
          Pulse.ar(
            SinOsc.kr(4).madd(1, 80),
            LFNoise1.kr(0.8).madd(0.4, 0.5)
          ) * 0.04,
          LFNoise1.kr(0.2).madd(2000, 2400),
//          LFNoise1.kr(0.2).madd(60.midicps, 60.midicps),
          0.2
        )

        val y = z * 0.6
        z + Seq(
          CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).madd(0.025, 0.035), 1)
            + CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).madd(0.025, 0.035), 1),
          CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).madd(0.025, 0.035), 1)
            + CombL.ar(y, 0.06, LFNoise1.kr(Rand(0, 0.3)).madd(0.025, 0.035), 1)
        )
        z
      }
    }
  }
}

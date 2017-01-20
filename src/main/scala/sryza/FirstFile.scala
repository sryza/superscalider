import java.awt.GridLayout

import de.sciss.synth._
import ugen.{EnvGen, _}
import Ops._

import scala.util.Random
import java.lang.Thread.sleep
import java.util.Scanner
import javax.swing._
import javax.swing.event.{ChangeEvent, ChangeListener}

/**
  * Plan:
  * We start with a scale.
  * We step through chords.
  * Each chord plays for N bars, then we pick a random next chord.
  * For each chord, we play 4 parallel repeating melodies.
  * Melodies are chosen as a sequence of random positions in the chord.
  */

/**
  * TODO: keep the same patterns, but move them down the scale
  */

object FirstFile {
  var playProb = 0.0
  var polyphony = 0

  def main(args: Array[String]): Unit = {
    val cfg = Server.Config()
    cfg.program = "/Applications/SuperCollider/SuperCollider.app/Contents/Resources/scsynth"
    Server.run(cfg) { s =>
      gui()

      new Thread() {
        override def run(): Unit = {
          playMusic()
        }
      }.start()

      val scanner = new Scanner(System.in)
      while (true) {
        playProb = scanner.nextDouble()
      }
    }
  }

  def gui(): Unit = {
    val rootPanel = new JPanel()
    rootPanel.setLayout(new GridLayout(1, 2))

    val playProbSlider = new JSlider(0, 10)
    playProbSlider.setValue(0)
    playProbSlider.setMajorTickSpacing(5)
    playProbSlider.setMinorTickSpacing(1)
    playProbSlider.setPaintTicks(true)
    playProbSlider.setPaintLabels(true)
    playProbSlider.setOrientation(SwingConstants.VERTICAL)
    playProbSlider.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent): Unit = {
        playProb = playProbSlider.getValue / 10.0
      }
    })

    val polyphonySlider = new JSlider(0, 5)
    polyphonySlider.setValue(0)
    polyphonySlider.setMajorTickSpacing(5)
    polyphonySlider.setMinorTickSpacing(1)
    polyphonySlider.setSnapToTicks(true)
    polyphonySlider.setPaintTicks(true)
    polyphonySlider.setPaintLabels(true)
    polyphonySlider.setOrientation(SwingConstants.VERTICAL)
    polyphonySlider.addChangeListener(new ChangeListener() {
      override def stateChanged(e: ChangeEvent): Unit = {
        polyphony = polyphonySlider.getValue
      }
    })

    rootPanel.add(playProbSlider)
    rootPanel.add(polyphonySlider)

    val frame = new JFrame()
    frame.setContentPane(rootPanel)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.pack()
    frame.setVisible(true)
  }

  def playMusic(): Unit = {
    val rand = new Random()

//    val scale = Seq(0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23)
    val scale = Seq(0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22)

    val notesPerMeasure = 8

    var chordRoot = 4
    while (true) {
      val chordRoot = scale(rand.nextInt(scale.length))
//      if (chordRoot == 3) {
//        chordRoot = 4
//      } else {
//        chordRoot = 3
//      }

      val chord = (0 until 4).map(x => (scale((chordRoot + x * 2) % scale.length)))
      val measures = 2 + rand.nextInt(2)
//      val measures = 2

      val patterns = Seq(
        ((0 until notesPerMeasure).map(x => chord(rand.nextInt(4)) - 24), "sin"),
        ((0 until notesPerMeasure).map(x => chord(rand.nextInt(4)) - 12), "saw"),
        ((0 until notesPerMeasure).map(x => chord(rand.nextInt(4))), "sin"),
        ((0 until notesPerMeasure).map(x => chord(rand.nextInt(4))), "saw"),
        ((0 until notesPerMeasure).map(x => chord(rand.nextInt(4)) + 12), "sin")
      )

      for (i <- 0 until measures) {
        for (j <- 0 until notesPerMeasure) {
          var sleepSum = 0
          for (((pattern, instrument), index) <- patterns.zipWithIndex) {
            if (index < polyphony) {
              val noteSleep = rand.nextInt(7)
              sleepSum += noteSleep
              sleep(noteSleep)
              if (rand.nextDouble() < playProb) {
                val decay = rand.nextDouble()
                val squarePower = rand.nextDouble()
                val sawPower = rand.nextDouble()

                val note = pattern(j)
                val p = play {
                  val tone =
//                    (if (instrument == "sin") {
                      SinOsc.ar((52 + note).midicps)
//                    } else {
//                      (LFPulse.ar((52 + note).midicps) * squarePower + Saw.ar((52 + note).midicps) * sawPower) * .5
//                    })
                   tone * EnvGen.kr(Env.perc(0.01, decay), doneAction = 2) * 0.2
                }
              }
            }
          }
          sleep(150 - sleepSum)
        }
      }
    }

  }
}


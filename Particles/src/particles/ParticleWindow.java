/*
 *  The MIT License
 *
 *  Copyright 2011 Andrew James <ephphatha@thelettereph.com>.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package particles;

import java.awt.Button;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Interactive window for the particle application.
 * <p>
 * This class manages the creation and destruction of threads and particles.
 *
 * @author Andrew James <ephphatha@thelettereph.com>
 */
public class ParticleWindow extends Frame
  implements ActionListener, MouseListener {

  private static final long serialVersionUID = -8674028275553402813L;
  private List<Thread> threads;
  private List<Particle> particles;
  private Thread controller;  //responsible for monitoring and culling threads
  private ParticleCanvas canvas;

  /**
   * Creates a thread to move the specified Particle.
   * <p>
   * Calls Particle.move() at approximately 10Hz. Also refreshes the display
   * each time the Particle is moved.
   *
   * @param p Particle to be managed
   * @return A thread object that when executed will move the particle
   */
  protected Thread makeThread(final Particle p) { // utility
    Runnable runloop = new Runnable() {

      public void run() {
        try {
          for (;;) {
            p.move();
            if (!p.inBounds(canvas.getWidth(), canvas.getHeight())) {
              return;
            }
            // canvas.repaint(); // Canvas is repainted by the controller
            // This prevents graphical artifacts caused by trying to repaint
            // the canvas too fast with large numbers of threads or large
            // canvas sizes.

            Thread.sleep(100); // 100msec is arbitrary
          }
        } catch (InterruptedException e) {
          return;
        }
      }
    };
    return new Thread(runloop);
  }

  /**
   * Initialises the particles/threads and controller then starts each thread.
   */
  public synchronized void start() {
    int n = 10; // just for demo

    if (threads == null) { // bypass if already started
      particles = new ArrayList<Particle>();
      for (int i = 0; i < n; ++i) {
        particles.add(new Particle(canvas.getWidth() / 2,
          canvas.getHeight() / 2));
      }
      canvas.setParticles(particles);

      threads = new ArrayList<Thread>();
      for (int i = 0; i < n; ++i) {
        threads.add(makeThread(particles.get(i)));
        threads.get(i).start();
      }
    }

    if (controller == null) {
      Runnable runloop = new Runnable() {

        public void run() {
          try {
            for (;;) {
              for (int i = 0; i < threads.size(); ++i) {
                if (!threads.get(i).isAlive()) {
                  threads.remove(i);
                  particles.remove(i);
                }
              }
              canvas.repaint(); // Repainting here to limit frame rate
              Thread.sleep(1000 / 60); // Roughly 60Hz
            }
          } catch (InterruptedException e) {
            return;
          }
        }
      };
      controller = new Thread(runloop);
      controller.start();
    }
  }

  /**
   * Kills all threads.
   * <p>
   * Interrupts and then drops the reference to the thread objects/collections
   * to allow the garbage collector to perform cleanup.
   */
  public synchronized void stop() {
    if (controller != null) {
      controller.interrupt();
      controller = null;
    }

    if (threads != null) { // bypass if already stopped
      for (int i = 0; i < threads.size(); ++i) {
        threads.get(i).interrupt();
      }
      threads = null;
    }
  }

  /**
   * Creates a window for the application.
   * <p>
   * Initial size is 300x300, with the title "Particle" and a single button
   * visible. Also registers the window to receive events from mouse clicks and
   * interaction with the button.
   */
  public ParticleWindow() {
    setTitle("Particles");
    setSize(300, 300);

    canvas = new ParticleCanvas(300);
    add("Center", canvas);

    Panel buttons = new Panel();
    Button spawnButton = new Button("Spawn");
    buttons.add(spawnButton);
    add("South", buttons);

    setVisible(true);

    canvas.createBufferStrategy(2);

    spawnButton.addActionListener(this);
    canvas.addMouseListener(this);
    addMouseListener(this);

    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        stop();
        System.exit(0);
      }
    });
  }

  /**
   * Handles events triggered by interaction with the Button.
   * <p>
   * Will spawn a Particle and Thread and add them to the relevant collections.
   *
   * @param evt The triggered event
   */
  public void actionPerformed(ActionEvent evt) {
    String command = evt.getActionCommand();
    if (command.equals("Spawn")) {
      Particle p = new Particle(canvas.getWidth() / 2, canvas.getHeight() / 2);
      Thread t = makeThread(p);

      synchronized (this) {
        particles.add(p);
        threads.add(t);
        t.start();
      }
    }
  }

  /**
   * Removes a Particle if a left click is detected within the Particle area.
   *
   * Only handles click events (mouse pressed and released without moving).
   * <p>
   * Iterates through all Particles until the first match is found then
   * removes that Particle and kills and removes the associated Thread before
   * returning. This ensures both collections remain synchronised.
   * <p>
   * If no hit is found, does nothing.
   *
   * @param e The state of the mouse when the event was triggered
   */
  public void mouseClicked(MouseEvent e) {
    if (e.getButton() == MouseEvent.BUTTON1) {
      int x = e.getX();
      int y = e.getY();

      synchronized (this) {
        for (int i = 0; i < particles.size(); ++i) {
          if (particles.get(i).nearPoint(x, y)) {
            threads.get(i).interrupt();
            threads.remove(i);
            particles.remove(i);
            return; //Only kill the first particle hit
          }
        }
      }
    }
  }

  /**
   * Does nothing.
   *
   * @param e Ignored
   */
  public void mousePressed(MouseEvent e) {
    return;
  }

  /**
   * Does nothing.
   *
   * @param e Ignored
   */
  public void mouseReleased(MouseEvent e) {
    return;
  }

  /**
   * Does nothing.
   *
   * @param e Ignored
   */
  public void mouseEntered(MouseEvent e) {
    return;
  }

  /**
   * Does nothing.
   *
   * @param e Ignored
   */
  public void mouseExited(MouseEvent e) {
    return;
  }
}

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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Canvas with a few extensions for managing display of Particles.
 * <p>
 * Based on the ParticleCanvas class provided by Lea.
 *
 * @author Andrew James <ephphatha@thelettereph.com>
 */
public class ParticleCanvas extends Canvas {

  private static final long serialVersionUID = 6150468117412136889L;
  private List<Particle> particles = new ArrayList<Particle>();
  private Image backbuffer;

  /**
   * Creates a square canvas of the specified size.
   * <p>
   * Not overly important what the size parameter is in this application as
   * the canvas size is modified as needed by the window.
   *
   * @param size Desired side length of the canvas
   */
  ParticleCanvas(int size) {
    setSize(new Dimension(size, size));
  }

  /**
   * Sets the reference to the list of Particles to be drawn on the canvas.
   *
   * @param ps The new Particle list
   */
  synchronized void setParticles(List<Particle> ps) {
    if (ps == null) {
      throw new IllegalArgumentException("Cannot set null");
    }

    particles = ps;
  }

  /**
   * Returns a reference to the list of Particles stored by the canvas.
   *
   * @return Reference to the list of particles
   */
  protected synchronized List<Particle> getParticles() {
    return particles;
  }

  /**
   * Paints the count of active Particles.
   * <p>
   * Also calls each Particles draw() method.
   *
   * @param g Graphics object to be used for output
   */
  @Override
  public void paint(Graphics g) { // override Canvas.paint
    List<Particle> ps = getParticles();

    g.setColor(Color.black);
    g.drawString(((Integer) (particles.size())).toString(), 0, 10);
    // One of the reasons I love Java. No function style casts...

    for (int i = 0; i < ps.size(); ++i) {
      ps.get(i).draw(g);
    }
  }

  /**
   * Creates a back buffer to be drawn on and calls paint(Graphics)
   * <p>
   * Double buffering code copied from:
   * http://www.ecst.csuchico.edu/~amk/classes/csciOOP/double-buffering.html
   * <p>
   * Has been slightly modified to suit my coding style and fix references to
   * deprecated methods.
   *
   * @param g Graphics object to be used for output
   */
  @Override
  public void update(Graphics g) {
    Graphics bb;
    Rectangle clip = g.getClipBounds();

    // create the off screen buffer and associated Graphics
    backbuffer = createImage(clip.width, clip.height);
    bb = backbuffer.getGraphics();
    // clear the exposed area
    bb.setColor(getBackground());
    bb.fillRect(0, 0, clip.width, clip.height);
    bb.setColor(getForeground());
    // do normal redraw
    bb.translate(-clip.x, -clip.y);
    paint(bb);
    // transfer off screen to window
    g.drawImage(backbuffer, clip.x, clip.y, this);

    backbuffer.flush();
  }
}

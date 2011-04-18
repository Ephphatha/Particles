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

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 * A simple particle with an x,y position and square shape.
 * <p>
 * Base on the Particle class provided by Lea
 *
 * @author Andrew James <ephphatha@thelettereph.com>
 */
public class Particle {

  protected final int HALFLENGTH = 5;
  protected int x;
  protected int y;
  protected final Random rng = new Random();  // Wasteful, but avoids blocking
  protected Color color;

  /**
   * Creates a Particle with the specified position.
   *
   * @param initialX Starting horizontal position
   * @param initialY Starting vertical position
   */
  public Particle(int initialX, int initialY) {
    x = initialX;
    y = initialY;
    color = new Color(rng.nextInt());
  }

  /**
   * Moves the Particle randomly.
   * <p>
   * Favors vertical motion, and is more likely to move up and to the left.
   * (Lea appears to call Random.nextInt(n) under the assumption that the range
   * returned is [0,n] when it is actually [0,n)
   */
  public synchronized void move() {
    x += rng.nextInt(10) - 5;   // Should be nextInt(11)
    y += rng.nextInt(20) - 10;  // Should be nextInt(21)
  }

  /**
   * Checks if the Particle is in the bounding rectangle.
   * <p>
   * Assumes the top left corner is at 0, 0 and the bottom right is at width,
   * height.
   *
   * @param width Width of bounding rectangle
   * @param height Height of bounding rectangle
   * @return True if the particle is partially contained within the rectangle
   */
  public synchronized Boolean inBounds(int width, int height) {
    // 10 is used as a buffer to ensure the particle is wholly outside the
    // bounding rect.
    final int BUFFER = 10;

    return (x + (HALFLENGTH + BUFFER) > 0
      && x - (HALFLENGTH + BUFFER) < width
      && y + (HALFLENGTH + BUFFER) > 0
      && y - (HALFLENGTH + BUFFER) < height);
  }

  /**
   * Checks if the Particle is near the specified point.
   *
   * @param x Horizontal position of the point
   * @param y Vertical position of the point
   * @param r Radius of the bounding circle
   * @return True if the particle is within radius of point (x,y)
   */
  public synchronized Boolean nearPoint(int x, int y, int r) {
    return (Math.pow(this.x - x, 2) < Math.pow(r, 2)
      && Math.pow(this.y - y, 2) < Math.pow(r, 2));
  }

  /**
   * Checks if the Particle is near the specified point.
   * <p>
   * Calls nearPoint(x, y, r) with r = the side length of the particle.
   *
   * @param x Horizontal position of the point
   * @param y Vertical position of the point
   * @return True if the particle is near point (x,y)
   */
  public synchronized Boolean nearPoint(int x, int y) {
    return nearPoint(x, y, HALFLENGTH * 2); // To make it easier to click
  }

  /**
   * Draws the particle at its current position.
   *
   * @param g Graphics device to use for output
   */
  public void draw(Graphics g) {
    int lx, ly;
    synchronized (this) {
      lx = x;
      ly = y;
    }
    g.setColor(color);
    g.fillRect(lx - HALFLENGTH, ly - HALFLENGTH,
      HALFLENGTH * 2, HALFLENGTH * 2);

    g.setColor(Color.BLACK);
    g.drawRect(lx - HALFLENGTH, ly - HALFLENGTH,
      HALFLENGTH * 2, HALFLENGTH * 2);
  }
}

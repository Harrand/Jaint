package io.github.harrand.jaint;

import java.util.HashMap;
import java.util.Map;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class Bitmap
{
	private double width, height;
	private Map<Point2D, Color> colour_matrix;
	
	/**	Instantiate a new Bitmap with existing dimensions.
	 * 
	 * @param dimensions - The enum value via which to construct the Bitmap.
	 */
	public Bitmap(SpriteDimensions dimensions)
	{
		switch(dimensions)
		{
		case x16:
			width = height = 16.0;
			break;
		case x32:
			width = height = 32.0;
			break;
		case x64:
			width = height = 64.0;
			break;
		default:
			width = height = 16.0;
			break;
		}
		
		this.colour_matrix = new HashMap<Point2D, Color>();
		for(int x = 0; x < this.width; x++)
		{
			for(int y = 0; y < this.height; y++)
			{
				this.colour_matrix.put(new Point2D(x, y), new Color(1.0, 1.0, 1.0, 1.0));
			}
		}
	}
	
	/** Set the pixel colour at a position in the Bitmap.
	 * 
	 * @param x - The x-coordinate of the Bitmap.
	 * @param y - The y-coordinate of the Bitmap.
	 * @param colour - The desired colour of Bitmap position [x, y]
	 */
	public void setPixel(int x, int y, Color colour)
	{
		this.colour_matrix.replace(new Point2D(x,y), colour);
	}
	
	/** Handles mouse-clicking in the Canvas that the Bitmap should be rendered to.
	 * 
	 * @param evt - The MouseEvent reference passed.
	 * @param paint_colour - The current paint-colour of the Sprite Editor in use.
	 * @param canvas_width - Width of the Canvas that the Bitmap should be rendered to.
	 * @param canvas_height - Height of the Canvas that the Bitmap should be rendered to.
	 */
	public void handleMouseClick(MouseEvent evt, Color paint_colour, double canvas_width, double canvas_height)
	{
		double block_width = canvas_width / this.width;
		double block_height = canvas_height / this.height;
		int x = (int) Math.floor(evt.getX() / block_width);
		int y = (int) Math.floor(evt.getY() / block_height);
		this.colour_matrix.replace(new Point2D(x, y), paint_colour);
	}
	
	/** Given an existing GraphicsContext, render the Bitmap into whatever is owning the context.
	 * 
	 * @param context - The context of the Canvas in which to render into.
	 */
	public void render(GraphicsContext context)
	{
		final double canvas_width = context.getCanvas().getWidth();
		final double canvas_height = context.getCanvas().getHeight();
		final double block_width = canvas_width / this.width;
		final double block_height = canvas_height / this.height;
		for(int x = 0; x < this.width; x++)
		{
			for(int y = 0; y < this.height; y++)
			{
				// Set fill colour to colour in current position in colour matrix.
				context.setFill(this.colour_matrix.get(new Point2D(x, y)));
				context.fillRect(x * block_width, y * block_height, block_width, block_height);
			}
		}
	}
	
	public double getWidth()
	{
		return this.width;
	}
	
	public double getHeight()
	{
		return this.height;
	}
}

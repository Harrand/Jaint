package io.github.harrand.jaint;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Jaint extends Application
{
	public static final double window_width = 512;
	public static final double window_height = 512;
	private SpriteDimensions sprite_dimensions;
	private Bitmap image_bitmap;
	private Color paint_colour;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	/** Instantiates the bitmap with the dimensions specified in this instance (default 16x16) and draws to the canvas.
	 * @param canvas - Reference to the canvas in which the Bitmap should be rendered to.
	 */
	public void create_new(Canvas canvas)
	{
		this.image_bitmap = new Bitmap(this.sprite_dimensions);
		this.update(canvas);
	}
	
	/** Opens an existing image with absolute path 'path'. ToggleGroup should contain all the radio toggles for dimensions, and the remaining parameters should be the toggles themselves aswell as the canvas to draw the image into.
	 * 
	 * @param path - The absolute path to the image to be imported.
	 * @param group - The ToggleGroup containing all the possible resolutions that the Sprite Editor supports.
	 * @param x16 - The toggle element for resolution 16x16.
	 * @param x32 - The toggle element for resolution 32x32.
	 * @param x64 - The toggle element for resolution 64x64.
	 * @param canvas - Reference to the canvas in which the imported image be rendered to.
	 */
	public void open_existing(String path, ToggleGroup group, Toggle x16, Toggle x32, Toggle x64, Canvas canvas)
	{
		this.sprite_dimensions = SpriteDimensions.x16;
		ImageView image_view = new ImageView(path);
		double width = image_view.getImage().getWidth();
		double height = image_view.getImage().getHeight();
		// Find closest supported resolution.
		final double average_size = (width + height) / 2.0;
		if(Math.abs(average_size - 32) < Math.abs(average_size - 16))
			this.sprite_dimensions = SpriteDimensions.x32;
		if(Math.abs(average_size - 64) < Math.abs(average_size - 32))
			this.sprite_dimensions = SpriteDimensions.x64;
		this.create_new(canvas);
		// Select the correct radio-button to reflect the size of the image imported.
		for(Toggle element : group.getToggles())
		{
			if(element.equals(x16) && this.sprite_dimensions == SpriteDimensions.x16)
				group.selectToggle(x16);
			else if(element.equals(x32) && this.sprite_dimensions == SpriteDimensions.x32)
				group.selectToggle(x32);
			else if(element.equals(x64) && this.sprite_dimensions == SpriteDimensions.x64)
				group.selectToggle(x64);
		}
		// Setup bitmap
		for(int x = 0; x < width; x++)
		{
			for(int y = 0; y < height; y++)
			{
				this.image_bitmap.setPixel(x, y, image_view.getImage().getPixelReader().getColor(x, y));
			}
		}
		this.update(canvas);
	}
	
	/** Export Bitmap data to the target file where the Bitmap data is stored in a Canvas.
	 * 
	 * @param target - The file in which to export the Bitmap data to.
	 * @param canvas - The canvas containing the Bitmap data.
	 */
	private void save(File target, Canvas canvas)
	{
		SnapshotParameters params = new SnapshotParameters();
		params.setFill(Color.TRANSPARENT);
		WritableImage snapshot = canvas.snapshot(params, null);
		String[] split = target.getName().split("\\.");
		String format = split[split.length - 1];
		try
		{
			// image is scaled and has the correct dimensions. However, a javaFX image does not contain writing methods, so its data must be drawn into a BufferedImage, which can be cast into an image with writing methods.
			Image image = SwingFXUtils.fromFXImage(snapshot, null).getScaledInstance((int)this.image_bitmap.getWidth(), (int)this.image_bitmap.getHeight(), Image.SCALE_FAST);
			BufferedImage buffer = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

			Graphics2D buffer_graphics = buffer.createGraphics();

			//draw 'image' directly into the buffer images graphics, performing a deep-copy.
			buffer_graphics.drawImage(image, null, null);
			buffer_graphics.dispose();
			ImageIO.write((RenderedImage) buffer, format, target);
			System.out.println("Successfully exported image with format " + format);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			System.out.println("Failed to export image: ");
			e.printStackTrace();
		}
	}
	
	/** Clear the canvas, and re-render the image Bitmap.
	 * 
	 * @param image_view - The canvas which contains the existing Bitmap data.
	 */
	public void update(Canvas image_view)
	{
		image_view.getGraphicsContext2D().clearRect(0, 0, image_view.getWidth(), image_view.getHeight());
		this.image_bitmap.render(image_view.getGraphicsContext2D());
	}

	@Override
	public void start(Stage stage)
	{
		// Default paint colour is black.
		this.paint_colour = Color.BLACK;
		stage.setTitle("Sprite Editor");
		// Initialise GUI elements
		Button button_new = new Button("New"), button_open = new Button("Open"), button_save = new Button("Save");
		RadioButton radio_16 = new RadioButton("16x16"), radio_32 = new RadioButton("32x32"), radio_64 = new RadioButton("64x64");
		ToggleGroup size_toggle = new ToggleGroup();
		size_toggle.getToggles().addAll(radio_16, radio_32, radio_64);
		size_toggle.selectToggle(radio_16);
		this.update_dimensions(size_toggle, radio_16, radio_32, radio_64);
		TextField colour_input = new TextField("000000");
		Label colour_label = new Label("Colour RGB (Hexadecimal)"), colour_display = new Label("Paint Colour");
		colour_display.setTextFill(Color.BLACK);
		colour_input.setOnKeyTyped(evt -> {if(colour_input.getText().length() != 6) return;this.paint_colour = Color.web(colour_input.getText());colour_display.setTextFill(this.paint_colour);});
		ToolBar toolbar_top = new ToolBar();
		toolbar_top.getItems().addAll(button_new, button_open, button_save);
		ToolBar toolbar_left = new ToolBar();
		toolbar_left.setOrientation(Orientation.VERTICAL);
		toolbar_left.getItems().addAll(radio_16, radio_32, radio_64, colour_label, colour_input, colour_display);
		BorderPane pane = new BorderPane();
		// Add CSS line to set the background colour of the pane to 0xe0e0e0 (light gray). This stops the canvas (which is white by default) from being hard to discern
		pane.setStyle("-fx-background-color: #e0e0e0;");
		pane.setTop(toolbar_top);
		pane.setLeft(toolbar_left);
		
		Canvas image_view = new Canvas(window_width - toolbar_left.getWidth(), window_height - toolbar_top.getHeight());
		image_view.setOnMouseClicked(evt -> {this.image_bitmap.handleMouseClick(evt, this.paint_colour, image_view.getWidth(), image_view.getHeight()); this.update(image_view);});
		button_new.setOnAction(evt -> {this.update_dimensions(size_toggle, radio_16, radio_32, radio_64); this.create_new(image_view);});
		button_open.setOnAction(evt -> {try{this.open_existing("file:///" + new FileChooser().showOpenDialog(stage).getAbsolutePath(), size_toggle, radio_16, radio_32, radio_64, image_view);}catch(Exception e) {}});
		button_save.setOnAction(evt -> {try{this.save(new File(new FileChooser().showSaveDialog(stage).getAbsolutePath()), image_view);}catch(Exception e) {}});
		pane.setCenter(image_view);
		image_view.autosize();
		stage.setScene(new Scene(pane, window_width, window_height));
		stage.show();
		// Add width and height padding of 50
		stage.setWidth(window_width + toolbar_left.getWidth() + 50);
		stage.setHeight(window_height + toolbar_top.getHeight() + 50);
		pane.setPrefWidth(stage.getWidth());
		pane.setPrefHeight(stage.getHeight());
		stage.setResizable(false);
		this.create_new(image_view);
	}
	
	/** Sets the SpriteDimensions enum value for this instance to be equal to whatever is chosen in a ToggleGroup.
	 * 
	 * @param group - The ToggleGroup containing all the possible resolutions that the Sprite Editor supports.
	 * @param x16 - The toggle element for resolution 16x16.
	 * @param x32 - The toggle element for resolution 32x32.
	 * @param x64 - The toggle element for resolution 64x64.
	 */
	private void update_dimensions(ToggleGroup group, Toggle x16, Toggle x32, Toggle x64)
	{
		Toggle select = group.getSelectedToggle();
		if(select.equals(x16))
			this.sprite_dimensions = SpriteDimensions.x16;
		else if(select.equals(x32))
			this.sprite_dimensions = SpriteDimensions.x32;
		else if(select.equals(x64))
			this.sprite_dimensions = SpriteDimensions.x64;
	}
}

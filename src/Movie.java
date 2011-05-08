package org.nlogo.extensions.qtj ;

import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.PrimitiveManager;
import org.nlogo.api.Syntax ;
import org.nlogo.api.Context ;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.Argument;
import org.nlogo.api.ExtensionException ;
import org.nlogo.api.LogoException ;

import quicktime.QTSession ;
import quicktime.qd.* ;
import quicktime.io.QTFile ;
import quicktime.util.RawEncodedImage ;

public strictfp class Movie
{
	private static quicktime.std.movies.Movie movie ;
	private static javax.swing.JFrame playerFrame ;
	private static QDGraphics graphics ;

	public static void unload() 
	{
		if( movie != null )
		{
			movie = null ;
			graphics = null ;
			if( playerFrame != null )
			{
				playerFrame.dispose() ;
				playerFrame = null ;
			}
			QTSession.close() ;
		}
	}

	public static class OpenMovie
		extends DefaultCommand
	{
		public Syntax getSyntax()
		{
			return Syntax.commandSyntax( new int [] { Syntax.TYPE_STRING , Syntax.TYPE_NUMBER , Syntax.TYPE_NUMBER } ) ;
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public void perform( Argument[] args , Context context )
			throws ExtensionException , LogoException
		{
			double patchSize = context.getAgent().world().patchSize() ;
			final float width = (float) ( args[ 1 ].getDoubleValue() * patchSize ) ;
			final float height = (float) ( args[ 2 ].getDoubleValue() * patchSize ) ;

			try
			{
				String filename = context.attachCurrentDirectory( args[ 0 ].getString() ) ;
				final java.io.File file = new java.io.File ( filename ) ;
				Runnable runnable = new Runnable(){
				public void run()
				{
					try
					{	
						QTSession.open() ;

						QDRect rect = new QDRect( width , height ) ;
						// workaround for intel macs (found from imagej)
						graphics = quicktime.util.EndianOrder.isNativeLittleEndian() 
							? new QDGraphics( QDConstants.k32BGRAPixelFormat , rect ) 
							: new QDGraphics( QDGraphics.kDefaultPixelFormat , rect ) ;

						quicktime.io.QTFile qtfile = new quicktime.io.QTFile( file ) ;
						quicktime.io.OpenMovieFile openMovieFile = quicktime.io.OpenMovieFile.asRead( qtfile ) ;
						movie = quicktime.std.movies.Movie.fromFile( openMovieFile ) ;
						movie.setGWorld( graphics , null ) ;
						movie.setBounds( rect ) ;
					}
					catch( quicktime.QTException e )
					{
						org.nlogo.util.Exceptions.handle( e ) ;
						//throw new ExtensionException ( e.getMessage() ) ;
					}
					} } ;
				((org.nlogo.window.GUIWorkspace)((org.nlogo.nvm.ExtensionContext)context).workspace()).waitFor( runnable ) ;
			}
			catch( java.io.IOException e )
			{
				throw new ExtensionException( e.getMessage() ) ;
			}
		}
	}

	public static class StartMovie
		extends DefaultCommand
	{
		public Syntax getSyntax()
		{
			return Syntax.commandSyntax( new int [] {} ) ;			
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public void perform( Argument [] args , Context context )
			throws ExtensionException , LogoException
		{
			if( movie == null )
			{
				throw new ExtensionException( "there is no movie open" ) ;
			}
			try
			{
				movie.start() ;
			}
			catch( quicktime.QTException e )
			{
				throw new ExtensionException ( e.getMessage() ) ;
			}
		}
	}

	public static class SetTime
		extends DefaultCommand
	{
		public Syntax getSyntax()
		{
			return Syntax.commandSyntax( new int [] { Syntax.TYPE_NUMBER } ) ;			
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public void perform( Argument [] args , Context context )
			throws ExtensionException , LogoException
		{
			if( movie == null )
			{
				throw new ExtensionException( "there is no movie open" ) ;
			}
			try
			{
				movie.setTimeValue( args[ 0 ].getIntValue() ) ;
			}
			catch( quicktime.QTException e )
			{
				throw new ExtensionException ( e.getMessage() ) ;
			}
		}
	}


	public static class OpenPlayer
		extends DefaultCommand
	{
		public Syntax getSyntax()
		{
			return Syntax.commandSyntax( new int [] {} ) ;			
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public void perform( Argument [] args , Context context )
			throws ExtensionException , LogoException
		{
			if( movie == null )
			{
				throw new ExtensionException( "there is no movie loaded" ) ;
			}
			try
			{
				java.awt.Component c = quicktime.app.view.QTFactory.makeQTComponent( movie ).asComponent() ;
				playerFrame = new javax.swing.JFrame() ;
				playerFrame.add( c ) ;
				QDRect bounds = movie.getBounds() ;
				playerFrame.setVisible( true ) ;
				playerFrame.setSize( new java.awt.Dimension( bounds.getWidth() , bounds.getHeight() ) ) ;
			}
			catch( quicktime.QTException e )
			{
				throw new ExtensionException ( e.getMessage() ) ;
			}
		}
	}

	public static class StopMovie
		extends DefaultCommand
	{
		public Syntax getSyntax()
		{
			return Syntax.commandSyntax( new int [] {} ) ;			
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public void perform( Argument [] args , Context context )
			throws ExtensionException , LogoException
		{
			if( movie == null )
			{
				throw new ExtensionException( "there is no movie loaded" ) ;
			}
			try
			{
				movie.stop() ;
			}
			catch( quicktime.QTException e )
			{
				throw new ExtensionException ( e.getMessage() ) ;
			}
		}
	}

	public static class CloseMovie
		extends DefaultCommand
	{
		public Syntax getSyntax()
		{
			return Syntax.commandSyntax( new int [] {} ) ;
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public void perform( Argument[] args , Context context )
			throws ExtensionException , LogoException
		{
			movie = null ;
			graphics = null ;
			if( playerFrame != null )
			{
				playerFrame.dispose() ;
				playerFrame = null ;
			}
			QTSession.close() ;
		}
	}

	public static class Image
		extends DefaultReporter
	{
		public Syntax getSyntax()
		{
			return Syntax.reporterSyntax
				( new int[] {} , Syntax.TYPE_WILDCARD ) ;
		}
		public String getAgentClassString()
		{
			return "O" ;
		}
		public Object report( Argument args[] , Context context )
			throws ExtensionException , LogoException
		{			
			try
			{
				Pict pict = movie.getPict( movie.getTime() ) ;
				pict.draw( graphics , movie.getBox() ) ;
				PixMap map = graphics.getPixMap() ;
				RawEncodedImage image = map.getPixelData() ;

				int intsPerRow = image.getRowBytes() / 4 ;
				int height = graphics.getBounds().getHeight() ;
				
				int [] data = new int[ intsPerRow * height ] ;
				image.copyToArray( 0 , data , 0 , data.length ) ;

				return QTJExtension.getBufferedImage( data , intsPerRow , height ) ;
			}
			catch( Exception e )
			{
				e.printStackTrace() ;
				throw new ExtensionException( e.getMessage() ) ;
			}
		}
	}
}

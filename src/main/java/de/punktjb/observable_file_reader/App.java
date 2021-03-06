package de.punktjb.observable_file_reader;


import rx.Observable;
import rx.Subscriber;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

/**
 * 
 * @author jurica
 *
 */
public class App 
{
	// buffer size
	final int bufferSize = 1*1024;

	// group size: email, name, surname
	final int groupSize = 3;
	
	final String[] groupOfStrings = new String[groupSize];
	int cnt = 0;
	
	/**
	 * 
	 * @param args
	 */
	public static void main( String[] args )
	{
		// pick one
		
		// simple and nice formated data
		//new App().simpleCase(args);
		
		// complex formated data
		new App().complexCase(args);
	}
	
	/**
	 * If it is possible that lines of text are non-existing or do not end with \n or \r 
	 * or it is not well formed - this solution shall be applied
	 * uses Rxjava Observables and lambdas from java 8 
	 * @param args
	 */
    public void complexCase( String[] args )
    {
    	
		if(args.length > 0 && args[0].equals("-f"))
		{
			Observable<char[]> fileContent = null;
			
//			final StringBuilder collected = new StringBuilder();
			
			// input titles
			try {
				
				// create observable on file stream with buffer of (x)*1024 characters
				fileContent = Observable.create(new ObservableBufferedFileStream(new FileReader(args[1]), bufferSize));
				
				// and start the processing (of stream)
				fileContent
					.map( charArray -> new String(charArray) )		// convert char array to string (because observable do not work with primitive types)
					.flatMap(string -> Observable.from(string.split("")))	// and then split to characters (but as strings)
					.filter(string -> !string.isEmpty())	// remove empty ones
					.filter(string -> !string.equals("\""))	// remove ["]
					.filter(string -> !string.equals("\r"))	// remove [carriage return]
					.lift( new Observable.Operator<String, String>() {	// using custom operator to collect characters to string

						@Override
						public Subscriber<? super String> call(Subscriber<? super String> child) {
							
							return new Subscriber<String>(child) {
								
								final StringBuilder collected = new StringBuilder();
								
				                @Override
				                public void onCompleted() {
				                	
				                		// push last content 
				                		if( collected.length() > 0 )
				                			child.onNext(collected.toString());	
				                	
				                		// and complete
				                    child.onCompleted();
				                }
				                
				                @Override
				                public void onError(Throwable e) {
				                	
				                    child.onError(e);
				                }
				                
				                @Override
				                public void onNext(String t) {
				                	
									// onNext or collect and request new
									if( ";\n".contains(t) && collected.length() > 0 ) {
										child.onNext(collected.toString());								
										collected.setLength(0);
									} else {
										collected.append(t);
										request(1);
									}
				                }
				            };
						}						
					})
					.subscribe(
							t -> storeString( t ),
							e -> System.err.println( "  ** error: " +  e.getMessage() ),
							() -> System.out.println( "  ** Done!" )
							);
//					.subscribe( 
//							t ->  {						
//									// collect characters
//									if( ";\n".contains(t) && collected.length() > 0 ) {
//										
//										// store string and reset buffer
//										storeString(collected.toString());								
//										collected.setLength(0);								
//									}								
//									else {
//										collected.append(t);
//									}
//							},
//							e -> System.err.println( "  ** error: " +  e.getMessage() ),
//							() -> System.out.println( "  ** Done!" )
//					);

			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println(" ** no arguments given");
		}
		
		return;
    }
		
    protected void storeString(final String string) {

    	groupOfStrings[cnt%groupSize] = string;
		
    	if(cnt++%groupSize == 2)
    		sendMail(groupOfStrings);
	}

	/**
     * If it is assumed that each line of file contains email address, name and sur name
     * this would be simple solution, uses streams and lambdas from java 8
     * 
     * @param args
     */
    public void simpleCase( String[] args )
    {

		if(args.length > 0 && args[0].equals("-f"))
		{
			try {
				BufferedReader bufferedReader = new BufferedReader(new FileReader(args[1]), bufferSize);
				
				bufferedReader.lines().forEach(
					s -> {
						// split and use to send
						String[] splitted = s.replaceAll("[\"]", "").split("[;]");						
						sendMail( splitted );
					}
				);
				
				bufferedReader.close();				
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		else
		{
			System.err.println(" ** no arguments given");
		}
		
		return;
    }

    /**
     * Email mock method
     * @param strings
     */
	protected void sendMail(String[] strings) {

		System.out.println(" ** sending email to " + strings[1] + " " + strings[2] + " <" + strings[0] + ">");
		
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException ignore) {}
	}

}

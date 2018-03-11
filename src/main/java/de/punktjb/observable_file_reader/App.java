package de.punktjb.observable_file_reader;


import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import rx.Observable;
import rx.Observer;

import java.io.FileReader;
import java.io.FileNotFoundException;

/**
 * 
 * @author jurica
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	
		if(args.length > 0 && args[0].equals("-f"))
		{
			Observable<char[]> content = null;
			
			// input titles
			try {
				int size = 1024*1024;
				
				// create observable on file stream with buffer of 1024*1024 characters
				content = Observable.create(new ObservableBufferedFileStream(new FileReader(args[1]), size));
				
				// and start the processing
				content
					.map( byteArray -> new String(byteArray) )	// convert byte array to string
					.map( string -> string.split(";"))			// split it by ";"
					.flatMapIterable( strings -> Arrays.<String>asList(strings))	// flatten map
					.filter(string -> !string.isEmpty())		// if string not empty
					.buffer(3)								// group them by 3: email, name, surname
					.subscribe( new Observer<List<String>>() {	// do something with it

						@Override
						public void onCompleted() {

							System.out.println( "  ** Done!" );
						}

						@Override
						public void onError(Throwable e) {

							System.err.println( "  ** error: " +  e.getMessage() );
							
						}

						@Override
						public void onNext(List<String> t) {

							// just print it for the time being
							t.stream().forEach(new Consumer<String>() {

								@Override
								public void accept(String t) {
									// TODO Auto-generated method stub
									System.err.println( "  ** found: " +  t );
								}
								
							});
						}						
					});
				
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}
		}
		else
		{
		
		}
    }
}

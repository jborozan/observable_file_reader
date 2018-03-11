package de.punktjb.observable_file_reader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import rx.Observer;
import rx.observables.SyncOnSubscribe;

/**
 *
 *
 */
public class ObservableBufferedFileStream extends SyncOnSubscribe<InputStreamReader, char[]> {

    private final InputStreamReader is;
    private final int size;

    public ObservableBufferedFileStream(InputStreamReader is, int size) {
    	
        this.is = is;
        this.size = size;
    }

	@Override
	protected InputStreamReader generateState() {

		return this.is;
	}

	@Override
	protected InputStreamReader next(InputStreamReader state, Observer<? super char[]> observer) {
		
	       char[] buffer = new char[size];
	        
	        try {
	            int count = is.read(buffer);
	            
	            if (count == -1) {
	                observer.onCompleted();
	            }
	            else if (count < size) {
	                observer.onNext(Arrays.copyOf(buffer, count));
	            }
	            else {
	                observer.onNext(buffer);
	            }
	        } catch (IOException e) {
	            observer.onError(e);
	        }
	        
	        return is;
	}

}

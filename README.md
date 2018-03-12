## observable_file_reader

Test task file reader.

There are two options:
- input data is well formated file: email, name, surname are found in each line
- input data is not well formated: there is possiblity to have one line of 1 GB or line with multiple triples in it

### Well formated file

It uses buffered reader to read the lines and then parses (splits) them

### Not so well formated file

It uses rxjava to first create buffered reader and then converts it to stream of characters. This stream will be processed 
and grouped to extract email, name, surname strings from it. Once this triple is extracted it "sends" email.

* To effectively send an email: maybe to group them and send bulk of emails (it is not so clear what does it mean).

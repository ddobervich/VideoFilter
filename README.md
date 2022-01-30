# VideoFilter
This repository contains a mac version of our video filter framework. Once cloned, please complete the following steps to ensure you can start working immediately without wasting time closing a bunch of pop-ups:

1. Copy the path of the `macosx64` folder inside <code>lib</code>.  ( the path is the string describing the folder location )
2. Open Terminal and excecute the following command:  
  `spctl --add <paste path here>/*`    (note: you must include the /* )
3. Copy the path of `gstreamer-1.0` that is inside `macosx64` Then excecute the command above.
4. In the terminal, excecute the command again but this time pasting the gstreamer-1.0 path:  
  `spctl --add <paste path here>/*`    (note: you must include the /* )
  
# That's It!

***Note: Windows and Linux machines can still display images with this framework, but can't use the webcam or videos***

If you are wondering what the command `spctl --add <paste path here>/*` does, Here is your answer:

The code above adds an exception to the macOS Security System that at default will not allow the `.dylib` and the `.so` files to run. Excecuting the command above will allow specifically the files inside the`lib` folder to run.

*If you have cloned this repository and completed all the steps above, and you still get a lot of pop-up dialogues, please add an Issue here, and I will do my best to resolve it.*

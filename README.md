# inferenceql.viz.stream

This web application allows users to view the results of streaming inference performed via an ensemble of Crosscat models. Users can move a slider which scrubs through iterations of inferenece and see how the state of the model ensemble and simulations from the ensemble change as more rows and columns of the dataset are incorporated and more inference is performed. Users can also interact with individual models as Javascript programs–-simulating data from them and seeing how different sections of the model (clusters) account for different subsets of the original dataset.   

## Building the app

The iql.viz.stream app is meant to be built automatically during runs of the `dvc-stream.yaml` pipeline file in the iql.auto-modeling repo. During a run of that streaming inference pipeline, various DVC stages will check out this repo, produce required data files, and compile a working copy of the app. Please see these various stages in `dvc-stream.yaml` for a detailed break down, but here are a few general notes about the stages.

- They make use of json streaming functions for less memory usage. For example `json/parse-stream` and `json/generate-stream`. Without this, various stages which produce data can fail because the JVM (used by Clojure) will run out of memory when using a large dataset or using a large ensemble.
* They make use of parallelization with `pmap` whenever possible.
* Generally, they attempt to precompute as much as possible for iql.viz.stream to have a smooth UX when scrubbing through iterations of streaming inference. 

## Data files needed by the app

In the end, the various stages in `dvc-stream.yaml` will produce the following data files and include them in the `temp-resources` directory of iql.viz.stream. Here is a breakdown. 

#### transitions.json

`js_array -> js_array -> transit_encode(xcat_latents)`

These are the xcat models the iql.viz.stream app uses to simulate data when a program cluster is clicked on the esemble-detail page. 

Each element in the outer array represents an iteration of inference. Each element in the inner array is an ensemble of models. The ensemble will be shortened to 3 models no matter how many were used during streaming inference. This saves space as we only display 3 models (Javascript programs) in the app. Each model is a map of latents needed to reify an xcat record. This was done because simply storing the xcat record would require repeating the dataset many times which would consume unneeded space. Lastly, these xcat_latents are transit_encoded to additionally save more space. 

NOTE: transit_encode() whenever mentioned produces a transit-encoded string. This is needed because the transit decode functions on the CLJS side expect a string and not a JSON object. This is possibly done for performance reasons.

#### transitions-samples.json

```
lz_string( 
	transit_encode( 
		clj_collection -> clj_collection -> clj_map
	)
)
```

These samples are displayed as simulated data in various plots in the app. This collection can be very large and on app startup it will need to fully reside in memory in order to enable smooth scrubbing through inference iterations. For these reasons, the collection is transit_encoded and then also compressed with lz_string. Lz_string compression can reduce the size by 10:1. 

The outer clj_collection has an entry for every iteration. The inner collection contains the samples at that iteration which are Clojure maps. The app by default produces 1000 samples at every iteration.   

#### mutual-info.json

`js_array -> js_obj -> js_obj -> number`

Mutual info is a misnomer here (see Github issue). This file actually contains the dependency probability between columns. The mutual-info name is a holdover from when mutual info was being used.

The outer array contains a entry for every iteration. js_obj is a Javascript object that is keyed by the first column name. Then the second js_obj is keyed by the second column name. The last number is the dependency probability between column-1 and column-2 ranging from 0 to 1.

#### js-program-transitions.json

`js_array -> js_array -> string`

This contains the Javascript text version of the xcat models at every iteration. The outer array has an element for every iteration. The inner array represents an ensemble of models. The inner most strings are Javascript program texts for each model in the ensemble.

## Other design considerations with data files 

### Getting data files into the app 

Because iql.viz.stream should be able to run without a server, the app cannot make async http requests for these data files. Instead, all of the previously mentioned data files are turned into Javascript source files via a simple script and required in the app's index.html. See `makefile` and `index.html` in this repo for more information on how this is done.

### Considerations regarding browser memory limits 

Each browser tab in Chrome is limited to about 2 GB. This has varied across time with different versions of Chrome. There is no way to change this. Other apps face this same issue. [See Figma](https://help.figma.com/hc/en-us/articles/360040528173-Reduce-memory-usage-in-files).

There are also other gotchas regrading memory. There are size limits on single Javascript source files and size limits on single objects in source files. So it's not a great idea to try and throw all of the data needed by the iql.viz.stream app into one big object in one file. The current approach of multiple data files might be better. 

All of the data files required by the app have to reside in memory contributing to the 2 GB limit. All of the samples are decompressed and reside in memory so that scrubbing through iterations can be smooth. The models however are only reified when needed to simulate from a particular cluster on the ensemble-detail page. Generally, the app stays under the memory requirements even with large datasets such as the full Beat19 dataset—using less that 1.5 GB memory.

### Initial app load time

The initial app load time is mostly due to decompressing all the samples in `transitions-sample.js` using lz-string. This necessary because smooth scrolling through transitions using the primary app slider would not be possible unless all the samples are available in memory.

## Other misc design considerations 

### Clickable clusters in Javascript program text

This feature currently works via the following steps.
- Use highlight.js to go from Javascript-program-string to html with special tags for syntax highlighting.
- Html from highlight.js is turned into hiccup and traversed to add additional div tags around cluster text to make them clickable.
- Hiccup is turned back into html and inserted into the dom.

Some alternatives were considered but were passed up.
- Alternative: Edit parser in highlight.js to add additional tags for clusters.  
- Alternative: Use Codewarrior to display Javascript program text. However, Codewarrior can only highlight specific lines of the program but not specific blocks of the code.

### Styling 
The Re-com library makes it easy to style re-frame components in-line. Unfortunately, not all of the app's components are styled in-line. Some are styled in the `style.css` file.

## Various useful bits
	
### Schemas for data
The app has a number of specs that can be used to understand the app.

- src/inferenceql/viz/stream/db.cljs  
	- This is where the state for all app interaction is stored.

- inferenceql/viz/stream/store.cljs
	- This is where the defs from the global namespace are brought into the app.

- inferenceql/viz/stream/config_spec.cljs
	- This is where specs for the app config are stored.

### Build levels

The app supports various levels of compilation similar to iql.viz, including using Figwheel to live develop the app. 

See [this document](https://docs.google.com/document/d/1EKhsDC1xRhSAYV2p_mUxr30Tn0L93CTTMCIbsez4DmA/edit?usp=sharing) for more info on the various build levels.


See the `makefile` for more details on all the following build commands.

- make js, js-advanced, js-advanced-min.
- make figwheel, make figwheel-10x

### Debugging 

The `dvc-stream.yaml` pipeline in iql.automodeling uses the `make js-advanced-min` build command which is difficult to debug due to code minification and CLJS advanced compilation. 

Try the following for better debugging.

- Use `make js` to build without minification or CLJS advanced compilation.
- Add `:enable-debug-interceptors true` to config.edn. 
  - This shows changes to the database in the browser console.
- Try `make figwheel-10x` to build which launches the app with re-frame-10x.

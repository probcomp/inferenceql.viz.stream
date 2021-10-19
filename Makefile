current-dir      := $(shell pwd)
output-dir       := $(current-dir)/out
output-dir-worker:= $(current-dir)/out-worker
src-dir          := $(current-dir)/src
resource-dir     := $(current-dir)/resources
cache-dir        := $(current-dir)/src/inferenceql/spreadsheets
node-modules-dir := $(current-dir)/node_modules

output-to	 := $(output-dir)/main.js

hot-css-file     := $(node-modules-dir)/handsontable/dist/handsontable.full.css
hot-css-resource := $(resource-dir)/css/handsontable.full.css
transitions-js := $(resource-dir)/transitions.js
transitions-json := $(resource-dir)/transitions.json
mutual-info-js := $(resource-dir)/mutual-info.js
mutual-info-json := $(resource-dir)/mutual-info.json

### Definitions for Figwheel.

figwheel-build-dir := $(current-dir)/figwheel-target
figwheel-public-dir := $(current-dir)/figwheel-target/public
figwheel-resource-dir := $(figwheel-public-dir)/resources
figwheel-index-file := $(figwheel-public-dir)/index.html

all: js

clean:
	rm -Rf $(output-dir)
	rm -Rf $(output-dir-worker)
	rm -Rf $(figwheel-build-dir)
	rm -Rf $(node-modules-dir)
	rm -Rf $(transitions-js)
	rm -Rf $(mutual-info-js)

### Spreadsheets app compilation.

compile-opts := $(current-dir)/compiler_options/app/build.edn
compile-opts-advn := $(current-dir)/compiler_options/app/build-advanced.edn
compile-opts-advn-min := $(current-dir)/compiler_options/app/build-advanced-min.edn

.PHONY: watch
watch: $(hot-css-resource)
	clojure -M -m cljs.main -w $(src-dir) -co $(compile-opts) -c inferenceql.viz.stream.core

.PHONY: watch-advanced
watch-advanced: $(hot-css-resource)
	clojure -M -m cljs.main -w $(src-dir) -co $(compile-opts-advn) -c inferenceql.viz.stream.core

.PHONY: watch-advanced-min
watch-advanced-min: $(hot-css-resource)
	clojure -M -m cljs.main -w $(src-dir) -co $(compile-opts-advn-min) -c inferenceql.viz.stream.core

.PHONY: js
js: $(hot-css-resource) $(transitions-js) $(mutual-info-js)
	clojure -M -m cljs.main -co $(compile-opts) -c inferenceql.viz.stream.core

.PHONY: js-advanced
js-advanced: $(hot-css-resource) $(transitions-js) $(mutual-info-js)
	clojure -M -m cljs.main -co $(compile-opts-advn) -c inferenceql.viz.stream.core

.PHONY: js-advanced-min
js-advanced-min: $(hot-css-resource) $(transitions-js) $(mutual-info-js)
	clojure -M -m cljs.main -co $(compile-opts-advn-min) -c inferenceql.viz.stream.core

### Supporting defs for compilation.

yarn-install-opts = --no-progress --frozen-lockfile

$(node-modules-dir): package.json yarn.lock
	yarn install $(yarn-install-opts)

$(hot-css-file): $(node-modules-dir)

$(hot-css-resource): $(hot-css-file)
	# Copy Handsontable CSS file from Handsontable NPM dependency.
	cp $(hot-css-file) $(resource-dir)/css

$(transitions-js): $(transitions-json)
	bin/js-ify-transitions $(transitions-json) $(transitions-js) transitions

$(mutual-info-js): $(mutual-info-json)
	bin/js-ify-transitions $(mutual-info-json) $(mutual-info-js) mutual_info

### Compilation with Figwheel

$(figwheel-public-dir):
	mkdir -p $(figwheel-public-dir)

$(figwheel-index-file): $(figwheel-public-dir)
	# Copy static index.html file.
	cp $(current-dir)/index.html $(figwheel-index-file)

$(figwheel-resource-dir): $(figwheel-public-dir) $(hot-css-resource) $(transitions-js) $(mutual-info-js)
	# Copy static resource files.
	cp -r $(resource-dir) $(figwheel-resource-dir)

# Deletes the entire figwheel build directory.
.PHONY: figwheel-clean
figwheel-clean: clean
	rm -Rf $(figwheel-build-dir)

# Deletes the static files in the figwheel build directory.
.PHONY: figwheel-clean-static
figwheel-clean-static:
	rm -Rf $(figwheel-resource-dir)
	rm -Rf $(figwheel-index-file)

# Creates fresh copy of static files. This is useful for updating CSS and other static
# files that have changed while figwheel is running.
.PHONY: figwheel-static
figwheel-static: figwheel-clean-static $(figwheel-public-dir) $(figwheel-resource-dir) $(figwheel-index-file)

figwheel-compile-opts := $(current-dir)/compiler_options/figwheel/build.edn
reframe-10x-compile-opts := $(current-dir)/compiler_options/reframe-10x/support.edn

## Starts the spreadsheets app using Figwheel.
.PHONY: figwheel
figwheel: figwheel-clean $(figwheel-resource-dir) $(figwheel-index-file)
	clojure -A:figwheel -M -m figwheel.main \
	-co  $(figwheel-compile-opts) \
	-c inferenceql.viz.stream.core --repl

## Starts the spreadsheets app using Figwheel and Re-frame-10x.
.PHONY: figwheel-10x
figwheel-10x: figwheel-clean $(figwheel-resource-dir) $(figwheel-index-file)
	clojure -A:figwheel:reframe-10x -M -m figwheel.main \
	-co $(figwheel-compile-opts):$(reframe-10x-compile-opts) \
	-c inferenceql.viz.stream.core --repl

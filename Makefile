build: clean
	mkdir out/
	javac -d out/ -cp src/ src/**/*.java
	./create_keys.sh

clean:
	rm -rf out/
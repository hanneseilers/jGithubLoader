# jGithubLoader
jGithubLoader is a Java toll for updateting Java applications from github repositories. It compares the currently installed application version with the latest release tag in github repository and downloads and compiles the latest tag release, if necessary.

jGithubLoader uses Github Java API (https://github.com/eclipse/egit-github/tree/master/org.eclipse.egit.github.core) for connecting to github repository.

## How to use
Include lates binary build from release downloads into your Java project or compile the jGithubLoader.jar file with Apache ant.

At the start of your programs main function, create a new instance of jGithubLoader and add your githubs username and repository name to the constructor. Than call the update function to start the update. The update function needs currently installed github tag name as argument.

```java
GithubLoader loader = new GithubLoader("hanneseilers", "jGithubLoader");
loader.update( "v0.0.0" );
```

## Prepare your project
To use jGithubLoader your project needs an ant build file called 'build.xml'. jGithubLoader will compile your application using this build file. It will compile your applications .jar-file to the same folder where the build file is. Take care that there are no other .jar-files. Otherwise the automatic update will not work.

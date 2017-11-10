### Protobuf Java stubs generator

Simple Maven plugin aimed for generating all the necessary java code for project which uses cross-platform Protocol Buffers serialization.

##### Usage

Add plugin repository:

```xml
    <pluginRepositories>
        <pluginRepository>
            <id>kishlaly</id>
            <url>http://maven.kishlaly.com</url>
        </pluginRepository>
    </pluginRepositories>
```

minimal configuration:

```xml
        <plugins>
            <plugin>
                <groupId>com.kishlaly.utils.maven</groupId>
                <artifactId>protobuf</artifactId>
                <version>1.0</version>
                <configuration>
                    <folders>
                        <folder>{where to search for *.proto files}</folder>
                        <folder>{where to search for another *.proto files}</folder>
                    </folders>
                </configuration>
                <executions>
                    <execution>
                        <phase>validate</phase>
                        <goals>
                            <goal>run</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
```

As a result you'll find generated Java classes in **targer/generated-sources** directory with all declared packages inside.

##### Optional configurations

Path to Protobuf compiler:
```xml
<!-- default: /usr/local/bin/protoc -->
<compiler></compiler>
```

Output directory:
```xml
<!-- default: generated-sources -->
<output></output>
```

If necessary to clean output directory before generating:
```xml
<!-- default: true -->
<clean></clean>
```

If necessary to stop the whole build in case of any errors. If false, damaged .proto file will be skipped.
```xml
<!-- default: true -->
<failFast></failFast>
```

### Notes

1. Specified **folder** will be analyzed recursively.
2. .proto files with the same names will be processed only once - plugin prohibits using not unique files. Need to think about this option, most probably it is normal to have the same proto files inside different directories, but with different _java_package_ and _java_outer_classname_ attributes.
3. You should use the correct **maven phase** in order to have all necessary generated sources before upcoming compilation phases, if you already have dependencies on generated code.
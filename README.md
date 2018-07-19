# Guitar Tabs Manager

## Setup
```
mvn clean package
sudo mkdir /usr/local/TabsManager
sudo cp ./target/tabs*.jar /usr/local/TabsManager/TabsManager.jar
sudo cp ./tabs /usr/sbin/
sudo chmod 755 /usr/sbin/tabs
```

## Usage
```
tabs phoenix
tabs --page <url>
```
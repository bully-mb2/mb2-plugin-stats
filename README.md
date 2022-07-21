# MB2 Stats
MB2 Stats is a plugin made for the [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader). The goal of this plugin is to track players across sessions and give plugin makers the power and tools they need to make persistent features.

# Prerequisites
1. [JRE](https://java.com/en/download/manual.jsp) that can run Java 11 or higher
2. [MB2 Log Reader](https://github.com/bully-mb2/mb2-log-reader)
3. [MariaDB](https://mariadb.org/) database

# Usage
Player commands: triggered by /say <command>
```
!balance <target>
    - Shows the targets credit balance, defaults to the player themselves
```

This app uses 4 tables to identify players across sessions:
1. account
   - Most important table for plugin makers
   - Stores UUID and balance
   - It's possible that clients on the same IP will use the same account
2. account_alias
   - Stores player aliases and first / last seen
   - Necessary for displaying usernames
3. account_ip
   - Links an IP to a UUID
   - First point of identification for a user
   - IP can link to multiple UUID, last seen is stored to help resolve account "stealing"
4. account_jaguid
   - Links a ja_guid to a UUID
   - Optional point of identification for a user
   - Overrides IP identification
   - More reliable
   - Only one UUID per ja_guid

# Configuration
If you want to update player names constantly you can enable the ClientUserinfoChanged event in the log reader
```
parser.disable.clientuserinfochanged=false
```

# Running
```
java -jar mb2-plugin-stats-VERSION.jar
```
After your first run a settings file will be generated next to the jar. Fill your credentials there and run again.

# Developing
To start developing generate your sources by running 
```
./mvn jaxb2:generate
```
Run this command every time the schema updates

## License
MB2 Stats is licensed under GPLv2 as free software. You are free to use, modify and redistribute MB2 Stats following the terms in LICENSE.txt
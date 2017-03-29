# snowflake
based on twitter snowflake, changed a little.  

### changes
* class -> enum: simplest singleton ThreadSafe
* node id Configurable: 

   ```instance(Integer.parseInt(System.getProperty("instance.id"))); ```
   
   VM options: ```-Dinstance.id=1``` (1~1023, according to your situation)
   
   Of course, u can write directly in the code:
   
   ```instance(1); // 1~1023 ```

### How to use
So Easy: ```Long id = IdWorker.instance.getId();```

see [Test](test/io/jacy/common/utils/IdWorkerTest.java)

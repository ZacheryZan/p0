package example
package models
package tour

import scala.collection.immutable.IndexedSeq
import scala.io.Source
import example.Helpers._
import org.mongodb.scala._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model.Accumulators._
import org.mongodb.scala.model._
import scala.collection.JavaConverters._
import net.liftweb.json._
import scala.io.StdIn.readChar
import scala.io.StdIn.readLine
import scala.io.BufferedSource
import com.mongodb.client.result.UpdateResult
import com.mongodb.client.result.DeleteResult


/*
XX 
XX Process File
XX User Task/Request Prompting
XX 
*/
object Main extends App {
    val client: MongoClient = MongoClient()
    val database: MongoDatabase = client.getDatabase("test")
    val collection: MongoCollection[Document] = database.getCollection("playDemo")
    Thread.sleep(2000)

    /*Open file, input content to list and building array of documents, insert documents into database*/
    //Setting up database/Inserting Documents
    val file = Source.fromFile("C:\\Users\\ZGree\\OneDrive\\Documents\\RevatureProjects\\new\\p0\\src\\main\\scala\\Project0_BikeData.txt")
    val docsList = file.getLines().toList
    var documentString = docsList.apply(0).split(",")
    var documents: Array[Document] = new Array[Document](docsList.length)
    documents(0) = Document("SPIN" -> documentString.apply(0).toInt, "Location" -> documentString.apply(1), "Racked" -> documentString.apply(2).toBoolean)
    for(x <- 1 until docsList.length)
    {
        documentString = docsList.apply(x).split(",")
        //print(documentString(0) + " " + documentString(1) + " " + documentString(2) + "\n")
        documents(x) = Document("SPIN" -> documentString.apply(0).toInt, "Location" -> documentString.apply(1), "Racked" -> documentString.apply(2).toBoolean)
    }
    print("\nInserting\n")
    val insertObservable: Observable[Completed] = collection.insertMany(documents)
    insertObservable.subscribe(new Observer[Completed]
    {
        override def onNext(result: Completed): Unit = println("inserted")
        override def onError(e: Throwable): Unit = println("Failed\n" + e + "\n")
        override def onComplete(): Unit = println("Completed")
    })
    Thread.sleep(4000)
    
    /*Main Block*/
    //Menu Options. Rent bike, Update status of Racked.
    //              Return bike, Update status of Racked.
    //collection.aggregate(List(group("$Location"))).printResults()

    var actionSelec: Char = '0'
    print("Welcome!\n")
    while(actionSelec != 'q')
    {
        print("Select..." + "\n(1)Rent\n(2)Return\n(q)Exit\n")
        actionSelec = readChar()
        try
            {
            actionSelec match
            {
                case '1' => 
                    print("\nEnter Location(state abbr)\n")
                    collection.find(and(equal("Location", readLine()), equal("Racked", true))).printResults()
                    print("Please Select Bike\n")           
                    collection.updateOne(equal("SPIN", readLine().toInt), set("Racked", false)).subscribe((updateResult: UpdateResult) => println(updateResult))
                    Thread.sleep(2000)
                    print("\nSELECTED.\nReady to Roll!\n")
                case '2' => 
                    collection.find(equal("Racked", false)).printResults()
                    print("\nSelect bike for return\n")
                    collection.updateOne(equal("SPIN", readLine().toInt), set("Racked", true)).subscribe((updateResult: UpdateResult) => println(updateResult))
                    Thread.sleep(2000)
                    print("Enjoyed the ride. You're Welcome")                   
                case 'q' => 
                    print("Roll on! Cycle back anytime!")
                case 'm' =>
                    var adm = 'm'
                    while(adm == 'm')
                    {
                    readChar() match 
                        {
                            case 'R' =>
                                collection.aggregate(List(group("$Racked", Accumulators.sum("Racked", 1)))).printResults()
                                Thread.sleep(2000)
                            case 'r' =>
                                collection.aggregate(List(group("$Location", Accumulators.sum("Bikes", 1)))).printResults()
                                collection.aggregate(List(group("$Location", Accumulators.sum("Bikes", 1)))).printResults()
                                Thread.sleep(2000)
                            case 'i' =>
                                var iDoc: Array[String] = readLine().split(" ")
                                collection.insertOne(Document("SPIN" -> iDoc(0).toInt, "Location" -> iDoc(1), "Racked" -> iDoc(2).toBoolean)).results();
                                Thread.sleep(2000)
                            case 'd' =>
                                collection.deleteOne(equal("SPIN", readLine().toInt)).printHeadResult("Delete Result: ")                       
                                Thread.sleep(2000)
                            case 't' =>
                                collection.find().printResults()
                            case 's' =>
                                collection.find(equal("SPIN", readLine().toInt)).printResults()
                            case 'n' =>
                                adm = 'n'
                            case _ =>
                                print("Hmm...")
                                Thread.sleep(2000)
                                print("\nn\ns\ni\nt\nd\nr\nR\n")
                        }
                    }
                //case _ => 
                //    print("Hmm..."); Thread.sleep(2000)
            }
        }
        catch
            {
                case e: Exception => print("Hmm...")
                Thread.sleep(2000)
            }
    }
        
    print("\nClosing\n")
    Thread.sleep(2000)


    client.close()
    Thread.sleep(2000)
}
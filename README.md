
# DEBS 2024 Grand Challenge — Real-Time Event Stream Processing System

This project is a solution to the **[DEBS Grand Challenge 2013](https://cmutschler.de/datasets/debs-2013-grand-challenge-soccer-monitoring)**, focusing on **real-time analysis of sensor data** in the context of football performance.

It leverages [Esper](http://www.espertech.com/esper/) for complex event processing (CEP) and is implemented in Java.

---

##  Queries Implemented

The system implements and tests the four core queries of the DEBS 2013 challenge:

1. **Query 1:** Real-time tracking of player running intensity  
2. **Query 2:** Player and team ball possession analysis  
3. **Query 3:** Heatmap generation for time spent in field zones  
4. **Query 4:** Detection of shots on goal  

 **Each query is explained in detail in the corresponding `explanationX.md` files.**  
You can find them as downloadable Markdown files in this project.

---

##  Research Paper

A comprehensive research paper has been written detailing the system architecture, the design and implementation of the Esper queries, and proposed directions for future work.

---

##  How to Run

This is a Maven project. To compile and run:

```bash
mvn clean package
java -cp target/demo-1.0-SNAPSHOT.jar com.example.Main
cd /demo
mvn exec:java
```
> Note: In EventSender.java, the input file path is specified on line 55. To run the project successfully, make sure you download the required data file and adjust the path accordingly if needed.
---

## 📬 Contact

Created by **Laia Ferrer**  
Thesis project (2024-2025 Second Semester)  

---

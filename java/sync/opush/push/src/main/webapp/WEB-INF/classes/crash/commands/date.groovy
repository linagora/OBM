class date {

  @Usage("Show the JVM date")
  @Command
  public void main() {
     out << new Date()
  }
}

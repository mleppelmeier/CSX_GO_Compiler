/**
 * Mallory Leppelmeier
 * 8/23/2020
 * Defines Symb Object
 */
class Symb {
   private String name;

   /**
    * Creates Symb
    * @param name The name to give the Symb
    */
   Symb(String name) {
      this.name = name;
   }

   /**
    * @return The name of the Symb
    */
   public String getName() {
      return name;
   }

   /**
    * @return String representation of the Symb
    */
   public String toString() {
      return name;
   }
} // class Symb

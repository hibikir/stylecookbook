package stylecookbook

import scala.concurrent.{Future, ExecutionContext}

object Futures {

  case class Employee(id:Int, name:String)
  case class Role(name:String, department :String)
  case class EmployeeWithRole(id :Int, name :String, role :Role)
  case class ExtendedEmployee(id :Int, name :String, role :Role, salary:BigDecimal)
  case class Department(name:String,employees:Seq[Int])

  trait EmployeeGrabberBabber {
    def department(name:String)(implicit ec:ExecutionContext) :Future[Department]
    def employee(id: Int)(implicit ec :ExecutionContext) :Future[Employee]
    def role(employee :Employee)(implicit ec :ExecutionContext) :Future[Role]
    def salary(employee:Employee)(implicit ec :ExecutionContext) : Future[BigDecimal]
  }


  class SampleMethods(grabber:EmployeeGrabberBabber)(implicit ec:ExecutionContext) {
    //avoid blocking
    //We can wait on a a future by using Await.response. However, This keeps our thread blocked, which makes it easier to
    //have performance problems. Instead, use Future composition features to return a single future holding the desired result

    def capitalizeName(id: Int): Future[String] =
      grabber.employee(id).map(_.name.capitalize)

    //for a single parameter, for loops are probably not worth it
    def capitalizeNameComprehension(id: Int) : Future[String] =
      for (e <- grabber.employee(id)) yield e.name.capitalize

    //When going through multiple futures, it the for loop often looks better

    def employeeWithRole(id: Int) : Future[EmployeeWithRole] =
      for (e <- grabber.employee(id);
           r <- grabber.role(e)) yield EmployeeWithRole(e.id, e.name, r)


    def capitalizedEmployee(e:Employee) : Employee = e.copy(name = e.name.capitalize)

    //however, if you use a for comprehension with futures, all the generators must return futures, or
    //it won't compile. For instance:

    /*
    def capitalEmployeeWithRole(id: Int) : Future[EmployeeWithRole] =
      for (e <- grabber.employee(id);
           c <- capitalizedEmployee(e);
           r <- grabber.role(e)) yield EmployeeWithRole(c.id, c.name, r)
    */

    //we'd have to either do the manipulation on the yield, or create a generator that returns a future, for instance:

    def capitalEmployeeWithRole(id: Int) : Future[EmployeeWithRole] =
      for (e <- grabber.employee(id);
           r <- grabber.role(e)) yield {
        val c = capitalizedEmployee(e)
        EmployeeWithRole(c.id, c.name, r)
      }

    def capitalizedFutureEmployee(e:Employee) :Future[Employee] = Future.successful(capitalizedEmployee(e))
    
    def capitalEmployeeWithRole2(id: Int) : Future[EmployeeWithRole] =
      for (e <- grabber.employee(id);
           c <- capitalizedFutureEmployee(e);
           r <- grabber.role(e)) yield EmployeeWithRole(c.id, c.name, r)

    //if two steps do not depend on each other the for loop will be slow, unless we create the futures ourselves

    def getExtendedInfo(e: Employee) = {
      val roleFuture = grabber.role(e)
      val salaryFuture = grabber.salary(e)
      for (r <- roleFuture;
           s <- salaryFuture) yield ExtendedEmployee(e.id, e.name, r, s)
    }

    //if we map instead of flatmap, then we'd get a Future[Future[ExtendedEmployee], which is cumbersome
    def extendedEmployee(id: Int) : Future[ExtendedEmployee] = {
      grabber.employee(id).flatMap(getExtendedInfo)
    }

    //Futures of sequences: in general, it's more polite to hand out a Future holding the sequence,
    //instead of a sequence of futures

    def employees(ids: Seq[Int]) : Future[Seq[Employee]] = {
      Future.traverse(ids)(grabber.employee)
    }

    //for comprehensions disentangle a lot of the problems of mixing futures and sequences
    def employeesByDept(dept: String) : Future[Seq[Employee]] =
      for (department <- grabber.department(dept);
           emps <- employees(department.employees)) yield emps
  }
}

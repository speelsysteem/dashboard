package controllers

import java.util.Date

import org.joda.time.{DateTimeZone, LocalDate}
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.db.slick._

import views._
import models._
import models.{Children => ChildrenModel}

object Children extends Controller {

  val jodaTzUTC: DateTimeZone = DateTimeZone.forID("UTC");

  implicit def dateToLocalDate(date: Option[Date]): Option[LocalDate] = date.map(d => new LocalDate(d.getTime(), jodaTzUTC))

  implicit def dateToLocalDate(date: Date) = new LocalDate(date.getTime(), jodaTzUTC)

  implicit def localDateToDate(localdate: Option[LocalDate]) = localdate.map(d =>
    new Date(d.toDateTimeAtStartOfDay(jodaTzUTC).getMillis())
  )

  implicit def localDateToDate(localdate: LocalDate) = new Date(localdate.toDateTimeAtStartOfDay(jodaTzUTC).getMillis())


  val childForm = Form(
    mapping(
      "id" -> optional(of[Long]),
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "mobilePhone" -> optional(text),
      "landline" -> optional(text),

      "street" -> optional(text),
      "city" -> optional(text),

      "birthDate" -> optional(date("dd-MM-yyyy")),
      "medicalRecordGood" -> boolean,
      "medicalRecordChecked" -> optional(date("dd-MM-yyyy"))
    )((id: Option[Long], firstName: String, lastName: String, mobilePhone: Option[String], landline: Option[String],
       street: Option[String], city: Option[String], birthDate: Option[java.util.Date], medRecGood: Boolean,
       medRecChecked: Option[java.util.Date]) => Child.apply(id, firstName, lastName, mobilePhone, landline, street,
      city, birthDate, medRecGood, medRecChecked)
      )(c =>
        c match {
          case Child(id, firstName, lastName, mobilePhone, landline, street, city, birthDate, medRecGood, medRecChecked) =>
            Some((id, firstName, lastName, mobilePhone, landline, street, city, birthDate.map(localDateToDate), medRecGood, medRecChecked.map(localDateToDate)))
          case _ => None
        }
      )
  )

  def showList = DBAction { implicit rs => Ok(html.child_list.render(ChildrenModel.findAll, rs.flash))}

  def newChild = Action { implicit rs => Ok(html.child_form.render(childForm, rs.flash))}

  def saveChild = DBAction { implicit rs =>
    childForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.child_form.render(formWithErrors, rs.flash)),
      child => {
        child.id match {
          case Some(x) => {
            ChildrenModel.update(child)
            Redirect(routes.Children.showList).flashing("success" -> "Kind upgedated")
          }
          case _ => {
            ChildrenModel.insert(child)
            Redirect(routes.Children.showList).flashing("success" -> "Kind toegevoegd")
          }
        }
      }
    )

  }

  def editChild(id: Long) = DBAction { implicit rs =>
    val child = ChildrenModel.findById(id)
    child match {
      case Some(ch) => Ok(html.child_form.render(childForm.fill(ch), rs.flash))
      case _ => BadRequest("Geen geldige id")
    }
  }

  def details(id: Long) = DBAction { implicit rs =>
    val child = ChildrenModel.findById(id)
    child match {
      case Some(x) => Ok(html.child_details(x))
      case None => BadRequest("Geen kind met die ID")
    }
  }
}
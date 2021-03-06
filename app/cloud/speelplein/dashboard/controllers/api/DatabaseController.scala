package cloud.speelplein.dashboard.controllers.api

import javax.inject.Inject

import cloud.speelplein.dashboard.controllers.actions.{
  TenantAction,
  GlobalTenantOnlyAction,
  JwtAuthorizationBuilder
}
import cloud.speelplein.dashboard.controllers.api.auth.Permission.listDatabases
import cloud.speelplein.data.TenantDatabaseService
import cloud.speelplein.models.JsonFormats.dbNameWrites
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext

class DatabaseController @Inject()(
    tenantAction: TenantAction,
    jwtAuthorizationBuilder: JwtAuthorizationBuilder,
    databaseService: TenantDatabaseService,
    globalTenantOnlyAction: GlobalTenantOnlyAction
)(implicit ec: ExecutionContext)
    extends ApiController {

  def list: Action[AnyContent] =
    (Action
      andThen tenantAction
      andThen globalTenantOnlyAction
      andThen jwtAuthorizationBuilder.authenticate(listDatabases)).async {
      req =>
        databaseService.all map { dbs =>
          Ok(Json.toJson(dbs))
        }
    }
}

package se.sundsvall.teamssender.api;

//@RestController
//@Tag(name = "Teams resource", description = "Resource for sending messages in Microsoft Teams")
//@Validated
//class TeamsResource {
//
//	@Autowired
//	private AuthenticationService authenticationService;
//
//	private final TeamsService teamsService;
//
//	public TeamsResource(final TeamsService teamsService) {
//		this.teamsService = teamsService;
//	}
//
//	@PostMapping("{municipalityId}/teams/messages")
//	@Operation(summary = "Send a message in Microsoft Teams", responses = {
//
//		@ApiResponse(
//			responseCode = "200",
//			description = "Message sent successfully",
//			useReturnTypeSchema = true),
//
//		@ApiResponse(
//			responseCode = "400",
//			description = "Invalid request payload or parameters",
//			content = @Content(schema = @Schema(oneOf = {
//				Problem.class,
//				ConstraintViolationProblem.class
//			}))),
//
//		@ApiResponse(
//			responseCode = "404",
//			description = "Chat could not be found or created",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "422",
//			description = "Message could not be created or sent",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "401",
//			description = "Authentication or authorization issue",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "503",
//			description = "Connection issue to Microsoft Graph API",
//			content = @Content(schema = @Schema(implementation = Problem.class))),
//
//		@ApiResponse(
//			responseCode = "500",
//			description = "Unexpected internal server error",
//			content = @Content(schema = @Schema(implementation = Problem.class)))
//	})
//
////	ResponseEntity<String> sendTeamsMessage(
////		@Parameter(name = "municipalityId", description = "Municipality ID", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
////
////		@Valid @RequestBody final SendTeamsMessageRequest request) {
////		AuthorizationCodeCredential credential = authenticationService.createCredential(session);
//
//		teamsService.sendMessage(municipalityId, request);
//		return ResponseEntity.ok("Message sent successfully");
//	}
//
//}

package ch.hsr.servicestoolkit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import ch.hsr.servicestoolkit.model.CouplingCriteriaVariant;
import ch.hsr.servicestoolkit.model.CouplingCriterion;
import ch.hsr.servicestoolkit.model.CouplingType;
import ch.hsr.servicestoolkit.model.EngineState;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.model.MonoCouplingInstance;
import ch.hsr.servicestoolkit.repository.CouplingCriteriaVariantRepository;
import ch.hsr.servicestoolkit.repository.CouplingCriterionRepository;
import ch.hsr.servicestoolkit.repository.DataFieldRepository;
import ch.hsr.servicestoolkit.repository.ModelRepository;
import ch.hsr.servicestoolkit.repository.MonoCouplingInstanceRepository;
import jersey.repackaged.com.google.common.collect.Lists;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private ModelRepository modelRepository;
	private DataFieldRepository dataRepository;
	private CouplingCriterionRepository couplingCriterionRepository;
	private CouplingCriteriaVariantRepository couplingCriteriaVariantRepository;
	private MonoCouplingInstanceRepository monoCouplingInstanceRepository;

	@Autowired
	public EngineService(final ModelRepository modelRepository, final DataFieldRepository dataRepository, final CouplingCriterionRepository couplingCriterionRepository,
			MonoCouplingInstanceRepository monoCouplingInstanceRepository, CouplingCriteriaVariantRepository couplingCriteriaVariantRepository) {
		this.modelRepository = modelRepository;
		this.dataRepository = dataRepository;
		this.couplingCriterionRepository = couplingCriterionRepository;
		this.monoCouplingInstanceRepository = monoCouplingInstanceRepository;
		this.couplingCriteriaVariantRepository = couplingCriteriaVariantRepository;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public EngineState message() {
		return new EngineState("Engine is up and running.");
	}

	@GET
	@Path("/models")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Model[] models() {
		List<Model> result = Lists.newArrayList(modelRepository.findAll());
		return result.toArray(new Model[result.size()]);
	}

	@GET
	@Path("/models/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Model getModel(@PathParam("id") final Long id) {
		return modelRepository.findOne(id);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/models/{id}/couplingcriteria")
	@Transactional
	public Set<MonoCouplingInstance> getModelCoupling(@PathParam("id") final Long id) {
		Set<MonoCouplingInstance> result = new HashSet<>();
		Model model = modelRepository.findOne(id);
		Set<MonoCouplingInstance> instances = monoCouplingInstanceRepository.findByModel(model.getId());
		result.addAll(instances);
		for (MonoCouplingInstance monoCouplingInstance : instances) {
			monoCouplingInstance.getDataFields().size(); // TODO find better
															// solution
		}
		log.debug("return criteria for model {}: {}", model.getName(), result.toString());
		return result;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/couplingcriteria")
	@Transactional
	public List<CouplingCriterionDTO> getCouplingCriteriaVariants() {
		List<CouplingCriterionDTO> result = new ArrayList<>();
		List<CouplingCriterion> list = Lists.newArrayList(couplingCriterionRepository.findAll());
		for (CouplingCriterion couplingCriterion : list) {
			result.add(new CouplingCriterionDTO(couplingCriterion, couplingCriteriaVariantRepository.readByCouplingCriterion(couplingCriterion)));
		}
		return result;
	}

	public class CouplingCriterionDTO {

		private final Long id;
		private final String name;
		private final String description;
		private final List<CouplingCriteriaVariant> variants;
		private final String decompositionImpact;
		private final CouplingType type;

		public CouplingCriterionDTO(CouplingCriterion couplingCriterion, List<CouplingCriteriaVariant> variants) {
			this.variants = variants;
			this.name = couplingCriterion.getName();
			this.id = couplingCriterion.getId();
			this.description = couplingCriterion.getDescription();
			decompositionImpact = couplingCriterion.getDecompositionImpact();
			type = couplingCriterion.getType();
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public List<CouplingCriteriaVariant> getVariants() {
			return variants;
		}

		public String getDescription() {
			return description;
		}

		public String getDecompositionImpact() {
			return decompositionImpact;
		}

		public CouplingType getType() {
			return type;
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/models/{id}/couplingcriteria")
	@Transactional
	public void storeCouplingCriteria(final Set<CouplingCriterion> criteria, @PathParam("id") final Long id) {
		// Model model = modelRepository.findOne(id);
		// TODO: rewrite if needed
		// for (CouplingCriterion inputCriterion : criteria) {
		// CouplingCriterion mappedСriterion = new CouplingCriterion();
		// for (DataField inputDataField : inputCriterion.getDataFields()) {
		// DataField dbDataField =
		// dataRepository.findByNameAndModel(inputDataField.getName(), model);
		// if (dbDataField == null) {
		// throw new IllegalArgumentException("referenced data field not
		// existing: " + model.getName() + ":" + inputDataField.getName());
		// }
		// mappedСriterion.getDataFields().add(dbDataField);
		// mappedСriterion.setCriterionType(inputCriterion.getCriterionType());
		// dbDataField.getCouplingCriteria().add(inputCriterion);
		// couplingCriterionRepository.save(mappedСriterion);
		// dataRepository.save(dbDataField);
		// log.debug("added coupling criterion {} to model {}",
		// inputCriterion.getId(), model.getName());
		// }
		// }
	}

	@POST
	@Path("/models")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Model createModel(Model model, @PathParam("modelName") final String modelName) {
		final String finalModelName = getNameForModel(model, modelName);
		if (model == null) {
			model = new Model();
		}
		model.setName(finalModelName);

		log.info("created model {} containing {} datafields.", finalModelName, model.getDataFields().size());
		modelRepository.save(model);
		return model;
	}

	private String getNameForModel(final Model model, final String name) {
		String modelName = (model == null || StringUtils.isEmpty(model.getName())) ? null : model.getName();
		if (modelName == null && StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("no name defined for model");
		} else if (modelName == null && !StringUtils.isEmpty(name)) {
			return name;
		} else if (modelName != null && StringUtils.isEmpty(name)) {
			return modelName;
		} else if (modelName.equals(name)) {
			return modelName;
		} else {
			throw new IllegalArgumentException("inconsistent model name in URI and body object");
		}
	}

}
